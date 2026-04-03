package semothon.team4.clothesup.analysis.dto;

import java.util.List;
import java.util.function.Function;
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

    public static AnalysisListItemResponse from(Analysis analysis, Grade grade, List<CareLabel> careLabels,
        String imageUrl, Function<String, String> presigner) {
        return AnalysisListItemResponse.builder()
            .id(analysis.getId())
            .name(analysis.getName())
            .category(analysis.getCategory())
            .imageUrl(imageUrl)
            .grade(grade)
            .careLabels(careLabels.stream()
                .map(cl -> CareLabelItem.builder()
                    .id(cl.getCareLabelList().getId())
                    .name(cl.getCareLabelList().getName())
                    .imageUrl(presigner.apply(cl.getCareLabelList().getImageUrl()))
                    .build())
                .toList())
            .build();
    }
}
