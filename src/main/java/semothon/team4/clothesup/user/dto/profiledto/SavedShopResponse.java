package semothon.team4.clothesup.user.dto.profiledto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedShopResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private String openTime;
    private String closeTime;
    private Double rate;
    private Long likeCount;
}
