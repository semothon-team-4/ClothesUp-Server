package semothon.team4.clothesup.shop.dto;

import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Shop;

@Getter
@Builder
public class ShopListResponse {
    private Long id;
    private String placeId;
    private String name;
    private String address;
    private Double lat;
    private Double lng;

    public static ShopListResponse from(Shop shop) {
        return ShopListResponse.builder()
            .id(shop.getId())
            .placeId(shop.getPlaceId())
            .name(shop.getName())
            .address(shop.getAddress())
            .lat(shop.getLat())
            .lng(shop.getLng())
            .build();
    }
}
