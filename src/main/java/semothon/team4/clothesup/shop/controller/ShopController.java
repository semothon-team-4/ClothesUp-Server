package semothon.team4.clothesup.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import semothon.team4.clothesup.global.common.BaseResponse;
import semothon.team4.clothesup.global.security.CustomUserDetails;
import semothon.team4.clothesup.shop.dto.ShopDetailResponse;
import semothon.team4.clothesup.shop.dto.ShopListResponse;
import semothon.team4.clothesup.shop.dto.ShopPriceDto;
import semothon.team4.clothesup.shop.dto.ShopPriceRegisterRequest;
import semothon.team4.clothesup.shop.dto.ShopRegisterRequest;
import semothon.team4.clothesup.shop.dto.ShopRegisterResponse;
import semothon.team4.clothesup.shop.service.ShopService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shops")
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "주변 세탁소 목록 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<List<ShopListResponse>>> getShopsNearby(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam double lat,
        @RequestParam double lng,
        @RequestParam(defaultValue = "1000") int radius
    ) {
        return BaseResponse.ok("세탁소 목록 조회 성공", shopService.getShopsNearby(lat, lng, radius));
    }

    @Operation(summary = "세탁소 상세 조회")
    @GetMapping("/{shopId}")
    public ResponseEntity<BaseResponse<ShopDetailResponse>> getShopDetail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long shopId
    ) {
        return BaseResponse.ok("세탁소 상세 조회 성공", shopService.getShopDetail(shopId));
    }

    @Operation(summary = "세탁소 등록")
    @PostMapping
    public ResponseEntity<BaseResponse<ShopRegisterResponse>> registerShop(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody ShopRegisterRequest request
    ) {
        return BaseResponse.created("세탁소 등록 성공", shopService.registerShop(request));
    }

    @Operation(summary = "세탁소 가격 목록 조회")
    @GetMapping("/{shopId}/prices")
    public ResponseEntity<BaseResponse<List<ShopPriceDto>>> getPrices(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long shopId
    ) {
        return BaseResponse.ok("가격 목록 조회 성공", shopService.getPrices(shopId));
    }

    @Operation(summary = "세탁소 가격 등록")
    @PostMapping("/{shopId}/prices")
    public ResponseEntity<BaseResponse<List<ShopPriceDto>>> registerPrices(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long shopId,
        @RequestBody ShopPriceRegisterRequest request
    ) {
        return BaseResponse.created("가격 등록 성공", shopService.registerPrices(shopId, request));
    }
}
