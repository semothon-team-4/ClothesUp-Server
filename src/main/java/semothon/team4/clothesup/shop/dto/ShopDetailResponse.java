package semothon.team4.clothesup.shop.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Shop;
import semothon.team4.clothesup.shop.domain.ShopPrice;

@Getter
@Builder
public class ShopDetailResponse {
    private Long id;
    private String placeId;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private List<ShopPriceDto> prices;
    private LocalDateTime createdAt;

    public static ShopDetailResponse from(Shop shop, List<ShopPrice> prices) {
        return ShopDetailResponse.builder()
            .id(shop.getId())
            .placeId(shop.getPlaceId())
            .name(shop.getName())
            .address(shop.getAddress())
            .lat(shop.getLat())
            .lng(shop.getLng())
            .prices(prices.stream().map(ShopPriceDto::from).toList())
            .createdAt(shop.getCreatedAt())
            .build();
    }
}
