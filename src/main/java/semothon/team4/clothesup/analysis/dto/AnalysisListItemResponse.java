package semothon.team4.clothesup.analysis.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.analysis.domain.CareLabel;
import semothon.team4.clothesup.analysis.domain.ConditionAnalysis.Grade;

@Getter
@Builder
public class AnalysisListItemResponse {

    private Long id;
    private String name;
    private String category;
    private String imageUrl;
    private Grade grade;
    private List<CareLabelItem> careLabels;

    @Getter
    @Builder
    public static class CareLabelItem {
        private Long id;
        private String name;
        private String imageUrl;
    }

    public static AnalysisListItemResponse from(Analysis analysis, Grade grade, List<CareLabel> careLabels) {
        return AnalysisListItemResponse.builder()
            .id(analysis.getId())
            .name(analysis.getName())
            .category(analysis.getCategory())
            .imageUrl(analysis.getImageUrl())
            .grade(grade)
            .careLabels(careLabels.stream()
                .map(cl -> CareLabelItem.builder()
                    .id(cl.getCareLabelList().getId())
                    .name(cl.getCareLabelList().getName())
                    .imageUrl(cl.getCareLabelList().getImageUrl())
                    .build())
                .toList())
            .build();
    }
}
