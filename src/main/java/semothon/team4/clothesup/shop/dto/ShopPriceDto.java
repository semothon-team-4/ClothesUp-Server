package semothon.team4.clothesup.shop.dto;

import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.ShopPrice;

@Getter
@Builder
public class ShopPriceDto {
    private Long id;
    private String category;
    private int price;
    private String priceGrade;

    public static ShopPriceDto from(ShopPrice shopPrice) {
        return ShopPriceDto.builder()
            .id(shopPrice.getId())
            .category(shopPrice.getCategory())
            .price(shopPrice.getPrice())
            .priceGrade(shopPrice.getPriceGrade())
            .build();
    }
}
