package semothon.team4.clothesup.user.dto.postdto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import semothon.team4.clothesup.user.domain.PostCategory;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private PostCategory category;
    private String title;
    private String content;
    private String authorNickname;
    private String authorProfileImage;
    private String analysisImageUrl;
    private String analysisName;
    private long likeCount;
    private long commentCount;
    private boolean isLiked;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
}
