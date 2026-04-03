package semothon.team4.clothesup.shop.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    public ReviewWriteResponse writeReview(User user, Long shopId, Long receiptId, int rating,
        String content, List<MultipartFile> images) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));
        Receipt receipt = (receiptId != null)
            ? receiptRepository.findById(receiptId)
                .orElseThrow(() -> new CoreException(ReceiptErrorCode.RECEIPT_NOT_FOUND))
            : null;

        Review review = reviewRepository.save(Review.builder()
            .user(user)
            .shop(shop)
            .receipt(receipt)
            .rating(rating)
            .content(content)
            .createdAt(LocalDateTime.now())
            .build());

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
