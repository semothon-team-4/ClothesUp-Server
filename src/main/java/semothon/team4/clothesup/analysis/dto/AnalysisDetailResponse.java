package semothon.team4.clothesup.analysis.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.analysis.domain.CareLabel;
import semothon.team4.clothesup.analysis.domain.CareLabelAnalysis;
import semothon.team4.clothesup.analysis.domain.ConditionAnalysis;

@Getter
@Builder
public class AnalysisDetailResponse {

    private Long id;
    private String name;
    private String category;
    private String imageUrl;
    private CareLabelDto careLabel;   // CareLabelAnalysis인 경우만 non-null
    private ConditionDto condition;   // ConditionAnalysis인 경우만 non-null
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class CareLabelDto {
        private Long id;
        private List<LabelItem> labels;

        @Getter
        @Builder
        public static class LabelItem {
            private Long id;
            private String name;
            private String imageUrl;
        }

        public static CareLabelDto from(CareLabelAnalysis careLabelAnalysis, List<CareLabel> careLabels) {
            return CareLabelDto.builder()
                .id(careLabelAnalysis.getId())
                .labels(careLabels.stream()
                    .map(cl -> LabelItem.builder()
                        .id(cl.getCareLabelList().getId())
                        .name(cl.getCareLabelList().getName())
                        .imageUrl(cl.getCareLabelList().getImageUrl())
                        .build())
                    .toList())
                .build();
        }
    }

    @Getter
    @Builder
    public static class ConditionDto {
        private Long id;
        private String grade;
        private int stainLevel;
        private int damageLevel;
        private String recommendation;

        public static ConditionDto from(ConditionAnalysis conditionAnalysis) {
            return ConditionDto.builder()
                .id(conditionAnalysis.getId())
                .grade(conditionAnalysis.getGrade().name())
                .stainLevel(conditionAnalysis.getStainLevel())
                .damageLevel(conditionAnalysis.getDamageLevel())
                .recommendation(conditionAnalysis.getRecommendation())
                .build();
        }
    }

    public static AnalysisDetailResponse fromCondition(Analysis analysis, ConditionAnalysis conditionAnalysis) {
        return AnalysisDetailResponse.builder()
            .id(analysis.getId())
            .name(analysis.getName())
            .category(analysis.getCategory())
            .imageUrl(analysis.getImageUrl())
            .condition(ConditionDto.from(conditionAnalysis))
            .careLabel(null)
            .createdAt(analysis.getCreatedAt())
            .build();
    }

    public static AnalysisDetailResponse fromCareLabel(Analysis analysis,
        CareLabelAnalysis careLabelAnalysis, List<CareLabel> careLabels) {
        return AnalysisDetailResponse.builder()
            .id(analysis.getId())
            .name(analysis.getName())
            .category(analysis.getCategory())
            .imageUrl(analysis.getImageUrl())
            .careLabel(CareLabelDto.from(careLabelAnalysis, careLabels))
            .condition(null)
            .createdAt(analysis.getCreatedAt())
            .build();
    }
}
