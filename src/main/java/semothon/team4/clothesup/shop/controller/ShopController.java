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
import semothon.team4.clothesup.shop.service.KakaoMapService;
import semothon.team4.clothesup.shop.service.ShopService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shops")
public class ShopController {

    private final ShopService shopService;
    private final KakaoMapService kakaoMapService;

    @Operation(summary = "지도 범위 내 세탁소 목록 조회")
    @GetMapping("/map")
    public ResponseEntity<BaseResponse<List<ShopListResponse>>> getShopsInBounds(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam Double swLat,
        @RequestParam Double swLng,
        @RequestParam Double neLat,
        @RequestParam Double neLng,
        @RequestParam Double lat,
        @RequestParam Double lng) {

        return BaseResponse.ok("세탁소 조회 성공",
            kakaoMapService.searchLaundryInBounds(swLat, swLng, neLat, neLng, lat, lng, userDetails != null ? userDetails.getUser() : null)
        );
    }

    @Operation(summary = "세탁소 상세 조회")
    @GetMapping("/{shopId}")
    public ResponseEntity<BaseResponse<ShopDetailResponse>> getShopDetail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long shopId
    ) {
        return BaseResponse.ok("세탁소 상세 조회 성공", shopService.getShopDetail(shopId, userDetails != null ? userDetails.getUser() : null));
    }

    @Operation(summary = "매장 관심 등록 토글")
    @PostMapping("/{shopId}/save")
    public ResponseEntity<BaseResponse<Boolean>> toggleSaveShop(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long shopId
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            return BaseResponse.unauthorized("인증 정보가 유효하지 않습니다.", null);
        }
        boolean isSaved = shopService.toggleSaveShop(shopId, userDetails.getUser());
        String message = isSaved ? "매장 저장 성공" : "매장 저장 취소 성공";
        return BaseResponse.ok(message, isSaved);
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
