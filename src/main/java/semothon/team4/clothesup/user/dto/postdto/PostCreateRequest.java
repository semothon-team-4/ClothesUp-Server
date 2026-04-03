package semothon.team4.clothesup.user.dto.postdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import semothon.team4.clothesup.user.domain.PostCategory;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    private Long analysisId;
    private boolean isPublic;
    @NotNull(message = "카테고리는 필수입니다.")
    private PostCategory category;
}
