package semothon.team4.clothesup.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.user.domain.Comment;
import semothon.team4.clothesup.user.domain.Post;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPost(Post post);
    long countByPost(Post post); // 댓글 수 조회 추가
}
