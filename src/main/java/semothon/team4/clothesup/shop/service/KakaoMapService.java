package semothon.team4.clothesup.shop.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import semothon.team4.clothesup.global.kakao.KakaoLocalApiClient;
import semothon.team4.clothesup.global.s3.S3Uploader;
import semothon.team4.clothesup.shop.domain.Shop;
import semothon.team4.clothesup.shop.dto.KakaoLocalDocument;
import semothon.team4.clothesup.shop.dto.KakaoSearchResponse;
import semothon.team4.clothesup.shop.dto.ShopListResponse;
import semothon.team4.clothesup.shop.repository.SavedShopRepository;
import semothon.team4.clothesup.shop.repository.ShopRepository;
import semothon.team4.clothesup.user.domain.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMapService {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofHours(1);

    private final KakaoLocalApiClient kakaoLocalApiClient;
    private final ShopRepository shopRepository;
    private final SavedShopRepository savedShopRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public List<ShopListResponse> searchLaundryInBounds(
        Double swLat, Double swLng,
        Double neLat, Double neLng,
        Double userLat, Double userLng,
        User user) {

        double centerLat = (swLat + neLat) / 2;
        double centerLng = (swLng + neLng) / 2;

        KakaoSearchResponse response =
            kakaoLocalApiClient.searchKeyword("세탁소", 15, centerLat, centerLng);

        if (response != null && response.getDocuments() != null) {
            response.getDocuments().stream()
                .filter(doc -> {
                    double lat = Double.parseDouble(doc.getY());
                    double lng = Double.parseDouble(doc.getX());
                    return lat >= swLat && lat <= neLat && lng >= swLng && lng <= neLng;
                })
                .forEach(this::upsertShop);
        }

        return shopRepository.findShopsInBounds(swLat, swLng, neLat, neLng).stream()
            .map(shop -> {
                boolean isSaved = user != null && savedShopRepository.existsByUserAndShop(user, shop);
                return ShopListResponse.from(shop, userLat, userLng,
                    shop.getImageUrl() != null ? s3Uploader.generatePresignedUrl(shop.getImageUrl(), PRESIGNED_URL_EXPIRATION) : null,
                    isSaved);
            })
            .toList();
    }

    private Shop upsertShop(KakaoLocalDocument doc) {
        return shopRepository.findByPlaceId(doc.getId())
            .orElseGet(() -> shopRepository.save(Shop.builder()
                .placeId(doc.getId())
                .name(doc.getPlaceName())
                .address(doc.getAddressName())
                .lat(Double.parseDouble(doc.getY()))
                .lng(Double.parseDouble(doc.getX()))
                .phone(doc.getPhone())
                .category(doc.getCategoryName())
                .placeUrl(doc.getPlaceUrl())
                .like(0L)
                .rate(0.0)
                .reviewCount(0L)
                .createdAt(LocalDateTime.now())
                .build()));
    }
}
