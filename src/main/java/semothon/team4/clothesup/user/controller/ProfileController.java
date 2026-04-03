package semothon.team4.clothesup.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import semothon.team4.clothesup.global.common.BaseResponse;
import semothon.team4.clothesup.global.security.CustomUserDetails;
import semothon.team4.clothesup.user.dto.postdto.PostListResponse;
import semothon.team4.clothesup.user.dto.profiledto.ReviewResponse;
import semothon.team4.clothesup.user.dto.profiledto.SavedShopResponse;
import semothon.team4.clothesup.user.dto.profiledto.UserProfileResponse;
import semothon.team4.clothesup.user.service.ProfileService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "내 프로필 조회", description = "내 프로필 정보와 관심 매장/글/리뷰 개수를 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<UserProfileResponse>> getMyProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileResponse response = profileService.getMyProfile(userDetails.getUser());
        return BaseResponse.ok("내 프로필 조회 성공", response);
    }

    @Operation(summary = "관심 매장 목록 조회", description = "내가 저장한 매장 목록을 조회합니다.")
    @GetMapping("/saved-shops")
    public ResponseEntity<BaseResponse<List<SavedShopResponse>>> getSavedShops(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<SavedShopResponse> responses = profileService.getSavedShops(userDetails.getUser());
        return BaseResponse.ok("관심 매장 목록 조회 성공", responses);
    }

    @Operation(summary = "관심 글(좋아요) 목록 조회", description = "내가 좋아요를 누른 게시글 목록을 조회합니다.")
    @GetMapping("/liked-posts")
    public ResponseEntity<BaseResponse<List<PostListResponse>>> getLikedPosts(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PostListResponse> responses = profileService.getLikedPosts(userDetails.getUser());
        return BaseResponse.ok("관심 글 목록 조회 성공", responses);
    }

    @Operation(summary = "내 리뷰 내역 조회", description = "내가 작성한 리뷰 목록을 조회합니다.")
    @GetMapping("/reviews")
    public ResponseEntity<BaseResponse<List<ReviewResponse>>> getMyReviews(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ReviewResponse> responses = profileService.getMyReviews(userDetails.getUser());
        return BaseResponse.ok("내 리뷰 내역 조회 성공", responses);
    }

    @Operation(summary = "매장 저장 토글", description = "매장을 관심 매장에 추가하거나 삭제합니다. (매장 좋아요 수 동기화)")
    @PostMapping("/saved-shops/{shopId}")
    public ResponseEntity<BaseResponse<Boolean>> toggleSaveShop(
        @PathVariable Long shopId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isSaved = profileService.toggleSaveShop(shopId, userDetails.getUser());
        String message = isSaved ? "매장 저장 성공" : "매장 저장 취소 성공";
        return BaseResponse.ok(message, isSaved);
    }
}
