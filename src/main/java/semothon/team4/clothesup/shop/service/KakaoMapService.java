package semothon.team4.clothesup.shop.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import semothon.team4.clothesup.global.kakao.KakaoLocalApiClient;
import semothon.team4.clothesup.shop.dto.KakaoSearchResponse;
import semothon.team4.clothesup.shop.dto.ShopPinResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMapService {

    private final KakaoLocalApiClient kakaoLocalApiClient;

    public List<ShopPinResponse> searchLaundryInBounds(
        double swLat, double swLng,
        double neLat, double neLng) {

        // sw/ne 중간값을 중심 좌표로 계산
        double centerLat = (swLat + neLat) / 2;
        double centerLng = (swLng + neLng) / 2;

        KakaoSearchResponse response =
            kakaoLocalApiClient.searchKeyword("세탁소", 15, centerLat, centerLng);

        if (response == null || response.getDocuments() == null) {
            return List.of();
        }

        return response.getDocuments().stream()
            .map(ShopPinResponse::fromKakaoDocument)
            .peek(shop -> log.info("name: {}, lat: {}, lng: {}",
                shop.getName(), shop.getLatitude(), shop.getLongitude()))
            .filter(shop -> isInBounds(shop, swLat, swLng, neLat, neLng))
            .toList();
    }

    private boolean isInBounds(ShopPinResponse shop,
        double swLat, double swLng,
        double neLat, double neLng) {
        return shop.getLatitude()  >= swLat && shop.getLatitude()  <= neLat
            && shop.getLongitude() >= swLng && shop.getLongitude() <= neLng;
    }
}