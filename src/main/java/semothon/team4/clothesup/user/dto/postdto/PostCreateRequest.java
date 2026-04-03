package semothon.team4.clothesup.user.dto.postdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import semothon.team4.clothesup.user.domain.PostCategory;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    private String title;
    private String content;
    private Long analysisId;
    private boolean isPublic;
    private PostCategory category;
}
