package semothon.team4.clothesup.shop.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Review;
@Getter
@Builder
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private String profileImage;
    private int rating;
    private String content;
    private List<String> images;
    private boolean isVerified; // 추가: 영수증 인증 여부
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review, List<String> presignedUrls, String profileImageUrl) {
        return ReviewResponse.builder()
            .id(review.getId())
            .userId(review.getUser().getId())
            .nickname(review.getUser().getNickname())
            .profileImage(profileImageUrl)
            .rating(review.getRating())
            .content(review.getContent())
            .images(presignedUrls)
            .isVerified(review.getReceipt() != null) // 영수증이 있으면 인증됨
            .createdAt(review.getCreatedAt())
            .build();
    }
}
