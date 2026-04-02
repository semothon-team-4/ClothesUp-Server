package semothon.team4.clothesup.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShopPinResponse {

    private String name;
    private String address;
    private String roadAddress;
    private String telephone;
    private Double latitude;
    private Double longitude;

    public static ShopPinResponse fromKakaoDocument(KakaoLocalDocument doc) {
        // 카카오 Local API는 WGS84 좌표를 문자열로 반환 (x=경도, y=위도)
        double longitude = Double.parseDouble(doc.getX());
        double latitude  = Double.parseDouble(doc.getY());

        return new ShopPinResponse(
            doc.getPlaceName(),
            doc.getAddressName(),
            doc.getRoadAddressName(),
            doc.getPhone(),
            latitude,
            longitude
        );
    }
}