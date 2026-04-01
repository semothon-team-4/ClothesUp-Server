package semothon.team4.clothesup.shop.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.shop.domain.Review;
import semothon.team4.clothesup.shop.domain.ReviewImage;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReview(Review review);
}
