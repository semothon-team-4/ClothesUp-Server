package semothon.team4.clothesup.user.dto.profiledto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String nickname;
    private String profileImage;
    private long savedShopCount;
    private long likedPostCount;
    private long reviewCount;
}
