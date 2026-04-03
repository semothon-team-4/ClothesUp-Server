package semothon.team4.clothesup.analysis.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.analysis.domain.CareLabel;
import semothon.team4.clothesup.analysis.domain.CareLabelAnalysis;
import semothon.team4.clothesup.analysis.domain.ConditionAnalysis;
import semothon.team4.clothesup.analysis.dto.AnalysisClosetResponse;
import semothon.team4.clothesup.analysis.dto.AnalysisDetailResponse;
import semothon.team4.clothesup.analysis.dto.AnalysisListItemResponse;
import semothon.team4.clothesup.analysis.repository.AnalysisRepository;
import semothon.team4.clothesup.analysis.repository.CareLabelAnalysisRepository;
import semothon.team4.clothesup.analysis.repository.CareLabelRepository;
import semothon.team4.clothesup.analysis.repository.ConditionAnalysisRepository;
import java.util.function.Function;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.AnalysisErrorCode;
import semothon.team4.clothesup.global.s3.S3Uploader;
import semothon.team4.clothesup.user.domain.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofHours(1);

    private final AnalysisRepository analysisRepository;
    private final ConditionAnalysisRepository conditionAnalysisRepository;
    private final CareLabelAnalysisRepository careLabelAnalysisRepository;
    private final CareLabelRepository careLabelRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public AnalysisDetailResponse requestAnalysis(User user, String name, String category, MultipartFile image) {
        String key = s3Uploader.upload(image, "analyses");

        Analysis analysis = analysisRepository.save(Analysis.builder()
            .user(user)
            .name(name)
            .category(category)
            .imageUrl(key)
            .createdAt(LocalDateTime.now())
            .build());

        // TODO: AI 분석 연동 시 아래 stub 데이터를 실제 분석 결과로 교체하세요
        ConditionAnalysis conditionAnalysis = conditionAnalysisRepository.save(
            ConditionAnalysis.builder()
                .analysis(analysis)
                .grade(ConditionAnalysis.Grade.A)
                .stainLevel(0)
                .damageLevel(0)
                .recommendation("분석 결과 없음")
                .build());

        String presignedUrl = s3Uploader.generatePresignedUrl(key, PRESIGNED_URL_EXPIRATION);
        return AnalysisDetailResponse.fromCondition(analysis, conditionAnalysis, presignedUrl);
    }

    @Transactional
    public AnalysisDetailResponse requestConditionAnalysis(
        User user, String name, String category, MultipartFile image,
        String grade, int stainLevel, int damageLevel, String recommendation, String storageTip
    ) {
        String key = s3Uploader.upload(image, "analyses");

        Analysis analysis = analysisRepository.save(Analysis.builder()
            .user(user)
            .name(name)
            .category(category)
            .imageUrl(key)
            .createdAt(LocalDateTime.now())
            .build());

        ConditionAnalysis conditionAnalysis = conditionAnalysisRepository.save(
            ConditionAnalysis.builder()
                .analysis(analysis)
                .grade(ConditionAnalysis.Grade.valueOf(grade.toUpperCase()))
                .stainLevel(stainLevel)
                .damageLevel(damageLevel)
                .recommendation(recommendation)
                .storageTip(storageTip)
                .build());

        String presignedUrl = s3Uploader.generatePresignedUrl(key, PRESIGNED_URL_EXPIRATION);
        return AnalysisDetailResponse.fromCondition(analysis, conditionAnalysis, presignedUrl);
    }

    public AnalysisClosetResponse getMyAnalyses(User user) {
        List<Analysis> analyses = analysisRepository.findByUser(user);

        // 등급 배치 조회
        Map<Long, ConditionAnalysis.Grade> gradeByAnalysisId =
            conditionAnalysisRepository.findByAnalysisIn(analyses).stream()
                .collect(Collectors.toMap(
                    ca -> ca.getAnalysis().getId(),
                    ConditionAnalysis::getGrade
                ));

        // 케어라벨 배치 조회
        List<CareLabelAnalysis> careLabelAnalyses =
            careLabelAnalysisRepository.findByAnalysisIn(analyses);

        Map<Long, CareLabelAnalysis> careLabelAnalysisByAnalysisId = careLabelAnalyses.stream()
            .collect(Collectors.toMap(
                cla -> cla.getAnalysis().getId(),
                cla -> cla
            ));

        Map<Long, List<CareLabel>> careLabelsByCareLabelAnalysisId =
            careLabelRepository.findByCareLabelAnalysisIn(careLabelAnalyses).stream()
                .collect(Collectors.groupingBy(cl -> cl.getCareLabelAnalysis().getId()));

        List<AnalysisListItemResponse> items = analyses.stream()
            .map(a -> {
                CareLabelAnalysis cla = careLabelAnalysisByAnalysisId.get(a.getId());
                List<CareLabel> labels = cla != null
                    ? careLabelsByCareLabelAnalysisId.getOrDefault(cla.getId(), List.of())
                    : List.of();
                String presignedUrl = s3Uploader.generatePresignedUrl(a.getImageUrl(), PRESIGNED_URL_EXPIRATION);
                return AnalysisListItemResponse.from(a, gradeByAnalysisId.get(a.getId()), labels, presignedUrl,
                    key -> s3Uploader.generatePresignedUrl(key, PRESIGNED_URL_EXPIRATION));
            })
            .toList();

        Map<ConditionAnalysis.Grade, Long> gradeCounts = items.stream()
            .filter(item -> item.getGrade() != null)
            .collect(Collectors.groupingBy(AnalysisListItemResponse::getGrade, Collectors.counting()));

        long careLabelCount = items.stream()
            .filter(item -> item.getGrade() == null)
            .count();

        return AnalysisClosetResponse.builder()
            .totalCount(analyses.size())
            .gradeCounts(gradeCounts)
            .careLabelCount(careLabelCount)
            .items(items)
            .build();
    }

    public AnalysisDetailResponse getAnalysisDetail(User user, Long analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new CoreException(AnalysisErrorCode.ANALYSIS_NOT_FOUND));

        if (!analysis.getUser().getId().equals(user.getId())) {
            throw new CoreException(AnalysisErrorCode.ANALYSIS_ACCESS_DENIED);
        }

        String presignedUrl = s3Uploader.generatePresignedUrl(analysis.getImageUrl(), PRESIGNED_URL_EXPIRATION);

        Function<String, String> presigner = key -> s3Uploader.generatePresignedUrl(key, PRESIGNED_URL_EXPIRATION);

        return conditionAnalysisRepository.findByAnalysis(analysis)
            .map(condition -> AnalysisDetailResponse.fromCondition(analysis, condition, presignedUrl))
            .orElseGet(() -> {
                CareLabelAnalysis cla = careLabelAnalysisRepository.findByAnalysis(analysis)
                    .orElseThrow(() -> new CoreException(AnalysisErrorCode.ANALYSIS_NOT_FOUND));
                List<CareLabel> labels = careLabelRepository.findByCareLabelAnalysis(cla);
                return AnalysisDetailResponse.fromCareLabel(analysis, cla, labels, presignedUrl, presigner);
            });
    }

    @Transactional
    public void deleteAnalysis(User user, Long analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new CoreException(AnalysisErrorCode.ANALYSIS_NOT_FOUND));

        if (!analysis.getUser().getId().equals(user.getId())) {
            throw new CoreException(AnalysisErrorCode.ANALYSIS_ACCESS_DENIED);
        }

        s3Uploader.delete(analysis.getImageUrl());

        careLabelAnalysisRepository.findByAnalysis(analysis).ifPresent(cla -> {
            careLabelRepository.deleteAll(careLabelRepository.findByCareLabelAnalysis(cla));
            careLabelAnalysisRepository.delete(cla);
        });
        conditionAnalysisRepository.findByAnalysis(analysis).ifPresent(conditionAnalysisRepository::delete);
        analysisRepository.delete(analysis);
    }
}
