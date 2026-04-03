package semothon.team4.clothesup.analysis.service;

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
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.AnalysisErrorCode;
import semothon.team4.clothesup.user.domain.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final ConditionAnalysisRepository conditionAnalysisRepository;
    private final CareLabelAnalysisRepository careLabelAnalysisRepository;
    private final CareLabelRepository careLabelRepository;

    @Transactional
    public AnalysisDetailResponse requestAnalysis(User user, String name, String category, MultipartFile image) {
        // TODO: 실제 파일 스토리지(S3 등) 연동 시 imageUrl 생성 로직을 교체하세요
        String imageUrl = image.getOriginalFilename();

        Analysis analysis = analysisRepository.save(Analysis.builder()
            .user(user)
            .name(name)
            .category(category)
            .imageUrl(imageUrl)
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

        return AnalysisDetailResponse.fromCondition(analysis, conditionAnalysis);
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
                return AnalysisListItemResponse.from(a, gradeByAnalysisId.get(a.getId()), labels);
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

        // ConditionAnalysis 우선 확인, 없으면 CareLabelAnalysis 확인
        return conditionAnalysisRepository.findByAnalysis(analysis)
            .map(condition -> AnalysisDetailResponse.fromCondition(analysis, condition))
            .orElseGet(() -> {
                CareLabelAnalysis cla = careLabelAnalysisRepository.findByAnalysis(analysis)
                    .orElseThrow(() -> new CoreException(AnalysisErrorCode.ANALYSIS_NOT_FOUND));
                List<CareLabel> labels = careLabelRepository.findByCareLabelAnalysis(cla);
                return AnalysisDetailResponse.fromCareLabel(analysis, cla, labels);
            });
    }

    @Transactional
    public void deleteAnalysis(User user, Long analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new CoreException(AnalysisErrorCode.ANALYSIS_NOT_FOUND));

        if (!analysis.getUser().getId().equals(user.getId())) {
            throw new CoreException(AnalysisErrorCode.ANALYSIS_ACCESS_DENIED);
        }

        careLabelAnalysisRepository.findByAnalysis(analysis).ifPresent(cla -> {
            careLabelRepository.deleteAll(careLabelRepository.findByCareLabelAnalysis(cla));
            careLabelAnalysisRepository.delete(cla);
        });
        conditionAnalysisRepository.findByAnalysis(analysis).ifPresent(conditionAnalysisRepository::delete);
        analysisRepository.delete(analysis);
    }
}
