package semothon.team4.clothesup.shop.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Review;
@Getter
@Builder
public class ReviewWriteResponse {
    private Long id;
    private Long shopId;
    private Long receiptId;
    private int rating;
    private String content;
    private List<String> images;
    private boolean isVerified; // 추가: 영수증 인증 여부
    private LocalDateTime createdAt;

    public static ReviewWriteResponse from(Review review, List<String> presignedUrls) {
        return ReviewWriteResponse.builder()
            .id(review.getId())
            .shopId(review.getShop().getId())
            .receiptId(review.getReceipt() != null ? review.getReceipt().getId() : null)
            .rating(review.getRating())
            .content(review.getContent())
            .images(presignedUrls)
            .isVerified(review.getReceipt() != null) // 영수증이 있으면 인증됨
            .createdAt(review.getCreatedAt())
            .build();
    }
}
