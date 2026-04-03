package semothon.team4.clothesup.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.user.domain.Post;
import semothon.team4.clothesup.user.domain.PostLike;
import semothon.team4.clothesup.user.domain.User;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
    long countByPost(Post post);
    long countByUser(User user);
    List<PostLike> findAllByUserOrderByCreatedAtDesc(User user);
}
