package semothon.team4.clothesup.user.dto.profiledto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private int rating;
    private String content;
    private List<String> imageUrls;
    private boolean isVerified;
    private LocalDateTime createdAt;
}
