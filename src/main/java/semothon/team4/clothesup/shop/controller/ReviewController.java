package semothon.team4.clothesup.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.global.common.BaseResponse;
import semothon.team4.clothesup.global.security.CustomUserDetails;
import semothon.team4.clothesup.shop.dto.ReviewResponse;
import semothon.team4.clothesup.shop.dto.ReviewWriteResponse;
import semothon.team4.clothesup.shop.service.ReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "세탁소 리뷰 목록 조회")
    @GetMapping("/shops/{shopId}/reviews")
    public ResponseEntity<BaseResponse<List<ReviewResponse>>> getReviews(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long shopId
    ) {
        return BaseResponse.ok("리뷰 목록 조회 성공", reviewService.getReviews(shopId));
    }

    @Operation(summary = "리뷰 작성")
    @PostMapping(value = "/shops/{shopId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ReviewWriteResponse>> writeReview(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long shopId,
        @RequestParam(required = false) MultipartFile receiptImage,
        @RequestParam int rating,
        @RequestParam String content,
        @RequestParam(required = false) List<MultipartFile> images
    ) {
        return BaseResponse.created("리뷰 작성 성공",
            reviewService.writeReview(userDetails.getUser(), shopId, receiptImage, rating, content, images));
    }

    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<BaseResponse<Void>> deleteReview(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(userDetails.getUser(), reviewId);
        return BaseResponse.ok("리뷰 삭제 성공", null);
    }
}
