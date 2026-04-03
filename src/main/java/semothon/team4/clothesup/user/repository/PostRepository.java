package semothon.team4.clothesup.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import semothon.team4.clothesup.user.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 최신순 조회
    List<Post> findAllByOrderByCreatedAtDesc();
    
    // 인기순 조회 (좋아요 많은 순)
    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON p = pl.post GROUP BY p ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<Post> findAllOrderByLikesDesc();
}
