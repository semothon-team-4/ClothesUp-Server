package semothon.team4.clothesup.shop.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class ShopPriceRegisterRequest {
    private List<PriceItem> prices;

    @Getter
    public static class PriceItem {
        private String category;
        private int price;
        private String priceGrade;
    }
}
