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
    List<Post> findAllByCategoryOrderByCreatedAtDesc(PostCategory category);
    
    // 인기순 조회 (좋아요 많은 순)
    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON p = pl.post GROUP BY p.id ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<Post> findAllOrderByLikesDesc();

    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON p = pl.post WHERE p.category = :category GROUP BY p.id ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<Post> findAllByCategoryOrderByLikesDesc(@Param("category") PostCategory category);

    // 최근 24시간 내 게시글 중 인기순 조회 (전체)
    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON p = pl.post " +
           "WHERE p.createdAt >= :since " +
           "GROUP BY p.id ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<Post> findPopularPostsSince(@Param("since") LocalDateTime since);

    // 최근 24시간 내 게시글 중 인기순 조회 (카테고리별)
    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON p = pl.post " +
           "WHERE p.category = :category AND p.createdAt >= :since " +
           "GROUP BY p.id ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<Post> findPopularPostsByCategorySince(@Param("category") PostCategory category, @Param("since") LocalDateTime since);

    // 검색 기능: 제목 또는 내용에 키워드가 포함된 게시글 조회
    List<Post> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(String titleKeyword, String contentKeyword);
    
    // 카테고리 내 검색
    List<Post> findByCategoryAndTitleContainingOrCategoryAndContentContainingOrderByCreatedAtDesc(PostCategory category1, String titleKeyword, PostCategory category2, String contentKeyword);
}
