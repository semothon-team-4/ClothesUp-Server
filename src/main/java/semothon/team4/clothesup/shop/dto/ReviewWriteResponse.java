package semothon.team4.clothesup.shop.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Review;
import semothon.team4.clothesup.shop.domain.ReviewImage;

@Getter
@Builder
public class ReviewWriteResponse {
    private Long id;
    private Long shopId;
    private Long receiptId;
    private int rating;
    private String content;
    private List<String> images;
    private LocalDateTime createdAt;

    public static ReviewWriteResponse from(Review review, List<ReviewImage> images) {
        return ReviewWriteResponse.builder()
            .id(review.getId())
            .shopId(review.getShop().getId())
            .receiptId(review.getReceipt().getId())
            .rating(review.getRating())
            .content(review.getContent())
            .images(images.stream().map(ReviewImage::getImageUrl).toList())
            .createdAt(review.getCreatedAt())
            .build();
    }
}
