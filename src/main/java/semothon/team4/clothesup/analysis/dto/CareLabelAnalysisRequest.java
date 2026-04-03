package semothon.team4.clothesup.analysis.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class CareLabelAnalysisRequest {

    private String name;
    private String category;
    private List<LabelItem> labels;

    @Getter
    public static class LabelItem {
        private String symbol;
        private String desc;
        private String imageBase64;
    }
}
