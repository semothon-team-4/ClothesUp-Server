package semothon.team4.clothesup.shop.service;

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

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ShopRepository shopRepository;
    private final ReceiptRepository receiptRepository;

    public List<ReviewResponse> getReviews(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));
        return reviewRepository.findByShop(shop).stream()
            .map(review -> ReviewResponse.from(review, reviewImageRepository.findByReview(review)))
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

        // TODO: 실제 파일 스토리지(S3 등) 연동 시 imageUrl 생성 로직을 교체하세요
        List<ReviewImage> savedImages = Collections.emptyList();
        if (images != null && !images.isEmpty()) {
            savedImages = reviewImageRepository.saveAll(
                images.stream()
                    .map(image -> ReviewImage.builder()
                        .review(review)
                        .imageUrl(image.getOriginalFilename())
                        .createdAt(LocalDateTime.now())
                        .build())
                    .toList()
            );
        }

        return ReviewWriteResponse.from(review, savedImages);
    }

    @Transactional
    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CoreException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new CoreException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }

        reviewImageRepository.deleteAll(reviewImageRepository.findByReview(review));
        reviewRepository.delete(review);
    }
}
