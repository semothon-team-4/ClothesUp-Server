package semothon.team4.clothesup.shop.dto;

import lombok.Getter;

@Getter
public class ShopRegisterRequest {
    private String placeId;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
}
