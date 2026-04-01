package semothon.team4.clothesup.shop.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.ShopErrorCode;
import semothon.team4.clothesup.shop.domain.Shop;
import semothon.team4.clothesup.shop.domain.ShopPrice;
import semothon.team4.clothesup.shop.dto.ShopDetailResponse;
import semothon.team4.clothesup.shop.dto.ShopListResponse;
import semothon.team4.clothesup.shop.dto.ShopPriceDto;
import semothon.team4.clothesup.shop.dto.ShopPriceRegisterRequest;
import semothon.team4.clothesup.shop.dto.ShopRegisterRequest;
import semothon.team4.clothesup.shop.dto.ShopRegisterResponse;
import semothon.team4.clothesup.shop.repository.ShopPriceRepository;
import semothon.team4.clothesup.shop.repository.ShopRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopPriceRepository shopPriceRepository;

    public List<ShopListResponse> getShopsNearby(double lat, double lng, int radius) {
        return shopRepository.findShopsWithinRadius(lat, lng, radius)
            .stream()
            .map(ShopListResponse::from)
            .toList();
    }

    public ShopDetailResponse getShopDetail(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));
        List<ShopPrice> prices = shopPriceRepository.findByShop(shop);
        return ShopDetailResponse.from(shop, prices);
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
