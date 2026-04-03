package semothon.team4.clothesup.analysis.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.analysis.domain.ConditionAnalysis.Grade;

@Getter
@Builder
public class AnalysisClosetResponse {

    private int totalCount;
    private Map<Grade, Long> gradeCounts;
    private long careLabelCount;
    private List<AnalysisListItemResponse> items;
}
