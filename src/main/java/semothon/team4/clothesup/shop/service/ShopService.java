package semothon.team4.clothesup.shop.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.ShopErrorCode;
import semothon.team4.clothesup.global.s3.S3Uploader;
import semothon.team4.clothesup.shop.domain.SavedShop;
import semothon.team4.clothesup.shop.domain.Shop;
import semothon.team4.clothesup.shop.domain.ShopPrice;
import semothon.team4.clothesup.shop.dto.ShopDetailResponse;
import semothon.team4.clothesup.shop.dto.ShopListResponse;
import semothon.team4.clothesup.shop.dto.ShopPriceDto;
import semothon.team4.clothesup.shop.dto.ShopPriceRegisterRequest;
import semothon.team4.clothesup.shop.dto.ShopRegisterRequest;
import semothon.team4.clothesup.shop.dto.ShopRegisterResponse;
import semothon.team4.clothesup.shop.repository.SavedShopRepository;
import semothon.team4.clothesup.shop.repository.ShopPriceRepository;
import semothon.team4.clothesup.shop.repository.ShopRepository;
import semothon.team4.clothesup.user.domain.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofHours(1);

    private final ShopRepository shopRepository;
    private final ShopPriceRepository shopPriceRepository;
    private final SavedShopRepository savedShopRepository;
    private final S3Uploader s3Uploader;

    public List<ShopListResponse> getShopsNearby(Double lat, Double lng, Integer radius, User user) {
        return shopRepository.findShopsWithinRadius(lat, lng, radius)
            .stream()
            .map(shop -> {
                boolean isSaved = user != null && savedShopRepository.existsByUserAndShop(user, shop);
                return ShopListResponse.from(shop, lat, lng, toPresignedUrl(shop.getImageUrl()), isSaved);
            })
            .toList();
    }

    public ShopDetailResponse getShopDetail(Long shopId, User user) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));
        List<ShopPrice> prices = shopPriceRepository.findByShop(shop);
        boolean isSaved = user != null && savedShopRepository.existsByUserAndShop(user, shop);
        return ShopDetailResponse.from(shop, prices, isSaved);
    }

    @Transactional
    public boolean toggleSaveShop(Long shopId, User user) {
        if (user == null) {
            throw new CoreException(semothon.team4.clothesup.global.exception.code.UserErrorCode.USER_NOT_FOUND);
        }
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));

        return savedShopRepository.findByUserAndShop(user, shop)
            .map(savedShop -> {
                savedShopRepository.delete(savedShop);
                savedShopRepository.flush();
                shop.updateLikeCount(-1);
                return false;
            })
            .orElseGet(() -> {
                savedShopRepository.save(new SavedShop(user, shop));
                shop.updateLikeCount(1);
                return true;
            });
    }

    private String toPresignedUrl(String key) {
        if (key == null) return null;
        return s3Uploader.generatePresignedUrl(key, PRESIGNED_URL_EXPIRATION);
    }

    @Transactional
    public ShopRegisterResponse registerShop(ShopRegisterRequest request) {
        Shop shop = Shop.builder()
            .placeId(request.getPlaceId())
            .name(request.getName())
            .address(request.getAddress())
            .lat(request.getLat())
            .lng(request.getLng())
            .createdAt(LocalDateTime.now())
            .build();
        return ShopRegisterResponse.from(shopRepository.save(shop));
    }

    public List<ShopPriceDto> getPrices(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));
        return shopPriceRepository.findByShop(shop)
            .stream()
            .map(ShopPriceDto::from)
            .toList();
    }

    @Transactional
    public List<ShopPriceDto> registerPrices(Long shopId, ShopPriceRegisterRequest request) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));
        List<ShopPrice> prices = request.getPrices().stream()
            .map(item -> ShopPrice.builder()
                .shop(shop)
                .category(item.getCategory())
                .price(item.getPrice())
                .priceGrade(item.getPriceGrade())
                .build())
            .toList();
        return shopPriceRepository.saveAll(prices)
            .stream()
            .map(ShopPriceDto::from)
            .toList();
    }
}
