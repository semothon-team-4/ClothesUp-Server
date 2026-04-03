package semothon.team4.clothesup.user.dto.postdto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private Long id;
    private String title;
    private String content;
    private String authorNickname;
    private String authorProfileImage;
    private String analysisImageUrl;
    private String analysisName;
    private long likeCount;
    private long commentCount;
    private boolean isLiked;
    private LocalDateTime createdAt;
}
