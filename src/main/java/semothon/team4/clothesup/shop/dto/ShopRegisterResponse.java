package semothon.team4.clothesup.shop.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Shop;

@Getter
@Builder
public class ShopRegisterResponse {
    private Long id;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private LocalDateTime createdAt;

    public static ShopRegisterResponse from(Shop shop) {
        return ShopRegisterResponse.builder()
            .id(shop.getId())
            .name(shop.getName())
            .address(shop.getAddress())
            .lat(shop.getLat())
            .lng(shop.getLng())
            .createdAt(shop.getCreatedAt())
            .build();
    }
}
