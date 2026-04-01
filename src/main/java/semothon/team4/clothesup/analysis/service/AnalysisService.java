package semothon.team4.clothesup.analysis.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.analysis.domain.CareLabel;
import semothon.team4.clothesup.analysis.domain.CareLabelAnalysis;
import semothon.team4.clothesup.analysis.domain.ConditionAnalysis;
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
        CareLabelAnalysis careLabelAnalysis = careLabelAnalysisRepository.save(
            CareLabelAnalysis.builder()
                .analysis(analysis)
                .build());

        ConditionAnalysis conditionAnalysis = conditionAnalysisRepository.save(
            ConditionAnalysis.builder()
                .analysis(analysis)
                .grade(ConditionAnalysis.Grade.A)
                .stainLevel(0)
                .damageLevel(0)
                .recommendation("분석 결과 없음")
                .build());

        List<CareLabel> careLabels = careLabelRepository.findByCareLabelAnalysis(careLabelAnalysis);

        return AnalysisDetailResponse.from(analysis, careLabelAnalysis, careLabels, conditionAnalysis);
    }

    public List<AnalysisListItemResponse> getMyAnalyses(User user) {
        return analysisRepository.findByUser(user).stream()
            .map(AnalysisListItemResponse::from)
            .toList();
    }

    public AnalysisDetailResponse getAnalysisDetail(User user, Long analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new CoreException(AnalysisErrorCode.ANALYSIS_NOT_FOUND));

        if (!analysis.getUser().getId().equals(user.getId())) {
            throw new CoreException(AnalysisErrorCode.ANALYSIS_ACCESS_DENIED);
        }

        CareLabelAnalysis careLabelAnalysis = careLabelAnalysisRepository.findByAnalysis(analysis)
            .orElseThrow(() -> new CoreException(AnalysisErrorCode.ANALYSIS_NOT_FOUND));
        List<CareLabel> careLabels = careLabelRepository.findByCareLabelAnalysis(careLabelAnalysis);
        ConditionAnalysis conditionAnalysis = conditionAnalysisRepository.findByAnalysis(analysis)
            .orElseThrow(() -> new CoreException(AnalysisErrorCode.ANALYSIS_NOT_FOUND));

        return AnalysisDetailResponse.from(analysis, careLabelAnalysis, careLabels, conditionAnalysis);
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
