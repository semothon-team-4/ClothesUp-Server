package semothon.team4.clothesup.analysis.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.analysis.domain.Analysis;

@Getter
@Builder
public class AnalysisListItemResponse {
    private Long id;
    private String name;
    private String category;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static AnalysisListItemResponse from(Analysis analysis) {
        return AnalysisListItemResponse.builder()
            .id(analysis.getId())
            .name(analysis.getName())
            .category(analysis.getCategory())
            .imageUrl(analysis.getImageUrl())
            .createdAt(analysis.getCreatedAt())
            .build();
    }
}
