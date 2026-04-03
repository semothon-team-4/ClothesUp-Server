package semothon.team4.clothesup.user.dto;

import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.user.domain.User;

@Getter
@Builder
public class UserProfileUpdateResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileImage;

    public static UserProfileUpdateResponse from(User user, String profileImageUrl) {
        return UserProfileUpdateResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .profileImage(profileImageUrl)
            .build();
    }
}
