package semothon.team4.clothesup.shop.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.shop.domain.Review;
import semothon.team4.clothesup.shop.domain.Shop;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByShop(Shop shop);
}
