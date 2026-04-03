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
public class CommentResponse {
    private Long id;
    private String content;
    private String authorNickname;
    private String authorProfileImage;
    private LocalDateTime createdAt;
}
