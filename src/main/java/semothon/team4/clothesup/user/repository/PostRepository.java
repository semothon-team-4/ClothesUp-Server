package semothon.team4.clothesup.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import semothon.team4.clothesup.user.domain.Post;
import semothon.team4.clothesup.user.domain.PostCategory;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 최신순 조회
    List<Post> findAllByOrderByCreatedAtDesc();
    
    // 카테고리별 최신순 조회
    List<Post> findAllByCategoryOrderByCreatedAtDesc(PostCategory category);

    // 검색 (제목/내용)
    List<Post> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(String title, String content);
    
    // 카테고리 내 검색 (제목/내용)
    List<Post> findByCategoryAndTitleContainingOrCategoryAndContentContainingOrderByCreatedAtDesc(PostCategory category1, String title, PostCategory category2, String content);

    // 인기순 조회 (좋아요 많은 순)
    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON p = pl.post GROUP BY p.id ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<Post> findAllOrderByLikesDesc();

    // 최근 24시간 내 게시글 중 인기순 조회
    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON p = pl.post " +
           "WHERE p.createdAt >= :since " +
           "GROUP BY p.id ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<Post> findPopularPostsSince(@Param("since") LocalDateTime since);

    // 특정 카테고리의 최근 24시간 인기글
    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON p = pl.post " +
           "WHERE p.category = :category AND p.createdAt >= :since " +
           "GROUP BY p.id ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<Post> findPopularPostsByCategorySince(@Param("category") PostCategory category, @Param("since") LocalDateTime since);
}
