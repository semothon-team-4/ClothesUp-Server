package semothon.team4.clothesup.shop.dto;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Shop;

@Getter
@Builder
public class ShopListResponse {

    private Long id;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private String imageUrl;
    private String category;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isOpen;
    private Long likeCount;
    private Double rate;
    private Long reviewCount;
    private Integer distance; // 미터 단위

    public static ShopListResponse from(Shop shop, double userLat, double userLng, String imageUrl) {
        Boolean isOpen = null;
        if (shop.getOpenTime() != null && shop.getCloseTime() != null) {
            LocalTime now = LocalTime.now();
            isOpen = !now.isBefore(shop.getOpenTime()) && !now.isAfter(shop.getCloseTime());
        }

        int distance = (int) Math.round(6371000 * Math.acos(
            Math.min(1.0,
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(shop.getLat()))
                    * Math.cos(Math.toRadians(shop.getLng()) - Math.toRadians(userLng))
                    + Math.sin(Math.toRadians(userLat)) * Math.sin(Math.toRadians(shop.getLat()))
            )
        ));

        return ShopListResponse.builder()
            .id(shop.getId())
            .name(shop.getName())
            .address(shop.getAddress())
            .lat(shop.getLat())
            .lng(shop.getLng())
            .imageUrl(imageUrl)
            .category(shop.getCategory())
            .openTime(shop.getOpenTime())
            .closeTime(shop.getCloseTime())
            .isOpen(isOpen)
            .likeCount(shop.getLike())
            .rate(shop.getRate())
            .reviewCount(shop.getReviewCount())
            .distance(distance)
            .build();
    }
}
