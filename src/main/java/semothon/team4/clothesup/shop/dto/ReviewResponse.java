package semothon.team4.clothesup.shop.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Review;
import semothon.team4.clothesup.shop.domain.ReviewImage;

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
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review, List<ReviewImage> images) {
        return ReviewResponse.builder()
            .id(review.getId())
            .userId(review.getUser().getId())
            .nickname(review.getUser().getNickname())
            .profileImage(review.getUser().getProfileImage())
            .rating(review.getRating())
            .content(review.getContent())
            .images(images.stream().map(ReviewImage::getImageUrl).toList())
            .createdAt(review.getCreatedAt())
            .build();
    }
}
