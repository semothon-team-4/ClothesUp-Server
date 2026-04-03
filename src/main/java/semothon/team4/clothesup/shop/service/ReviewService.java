package semothon.team4.clothesup.shop.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.ReceiptErrorCode;
import semothon.team4.clothesup.global.exception.code.ReviewErrorCode;
import semothon.team4.clothesup.global.exception.code.ShopErrorCode;
import semothon.team4.clothesup.global.s3.S3Uploader;
import semothon.team4.clothesup.shop.domain.Receipt;
import semothon.team4.clothesup.shop.domain.Review;
import semothon.team4.clothesup.shop.domain.ReviewImage;
import semothon.team4.clothesup.shop.domain.Shop;
import semothon.team4.clothesup.shop.dto.ReviewResponse;
import semothon.team4.clothesup.shop.dto.ReviewWriteResponse;
import semothon.team4.clothesup.shop.repository.ReceiptRepository;
import semothon.team4.clothesup.shop.repository.ReviewImageRepository;
import semothon.team4.clothesup.shop.repository.ReviewRepository;
import semothon.team4.clothesup.shop.repository.ShopRepository;
import semothon.team4.clothesup.user.domain.User;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofHours(1);

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ShopRepository shopRepository;
    private final ReceiptRepository receiptRepository;
    private final S3Uploader s3Uploader;

    public List<ReviewResponse> getReviews(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));
        return reviewRepository.findByShop(shop).stream()
            .map(review -> {
                List<String> presignedUrls = reviewImageRepository.findByReview(review).stream()
                    .map(img -> s3Uploader.generatePresignedUrl(img.getImageUrl(), PRESIGNED_URL_EXPIRATION))
                    .toList();
                String profileImageUrl = review.getUser().getProfileImage() != null
                    ? s3Uploader.generatePresignedUrl(review.getUser().getProfileImage(), PRESIGNED_URL_EXPIRATION)
                    : null;
                return ReviewResponse.from(review, presignedUrls, profileImageUrl);
            })
            .toList();
    }

    @Transactional
    public ReviewWriteResponse writeReview(User user, Long shopId, MultipartFile receiptImage, int rating,
        String content, List<MultipartFile> images) {
        
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));

        // 1. 영수증 이미지 유무 로그 출력
        if (receiptImage != null && !receiptImage.isEmpty()) {
            log.info("영수증 이미지 수신 성공: {}, 크기: {}", receiptImage.getOriginalFilename(), receiptImage.getSize());
        } else {
            log.warn("영수증 이미지가 전송되지 않았거나 비어있습니다.");
        }

        // 2. 영수증 처리
        Receipt receipt = null;
        if (receiptImage != null && !receiptImage.isEmpty()) {
            String receiptUrl = s3Uploader.upload(receiptImage, "receipts");
            receipt = receiptRepository.save(Receipt.builder()
                .user(user)
                .shop(shop)
                .imageUrl(receiptUrl)
                .createdAt(LocalDateTime.now())
                .build());
        }

        // 3. 리뷰 저장
        Review review = reviewRepository.save(Review.builder()
            .user(user)
            .shop(shop)
            .receipt(receipt)
            .rating(rating)
            .content(content)
            .createdAt(LocalDateTime.now())
            .build());

        // 4. 일반 리뷰 이미지 처리
        List<MultipartFile> validImages = (images == null) ? List.of()
            : images.stream().filter(f -> f != null && !f.isEmpty()).toList();

        List<ReviewImage> savedImages = Collections.emptyList();
        if (!validImages.isEmpty()) {
            savedImages = reviewImageRepository.saveAll(
                validImages.stream()
                    .map(image -> ReviewImage.builder()
                        .review(review)
                        .imageUrl(s3Uploader.upload(image, "reviews"))
                        .createdAt(LocalDateTime.now())
                        .build())
                    .toList()
            );
        }

        List<String> presignedUrls = savedImages.stream()
            .map(img -> s3Uploader.generatePresignedUrl(img.getImageUrl(), PRESIGNED_URL_EXPIRATION))
            .toList();

        // 5. 응답 생성 (영수증 객체를 직접 확인하여 확실하게 true/false 세팅)
        return ReviewWriteResponse.from(review, presignedUrls);
    }

    @Transactional
    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CoreException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new CoreException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }

        List<ReviewImage> reviewImages = reviewImageRepository.findByReview(review);
        reviewImages.forEach(img -> s3Uploader.delete(img.getImageUrl()));
        reviewImageRepository.deleteAll(reviewImages);
        reviewRepository.delete(review);
    }
}
