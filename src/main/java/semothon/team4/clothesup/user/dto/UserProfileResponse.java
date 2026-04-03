package semothon.team4.clothesup.user.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.user.domain.User;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileImage;
    private LocalDateTime createdAt;

    public static UserProfileResponse from(User user, String profileImageUrl) {
        return UserProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .profileImage(profileImageUrl)
            .createdAt(user.getCreatedAt())
            .build();
    }
}
