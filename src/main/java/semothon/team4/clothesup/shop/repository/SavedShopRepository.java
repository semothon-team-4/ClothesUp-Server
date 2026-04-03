package semothon.team4.clothesup.shop.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.shop.domain.SavedShop;
import semothon.team4.clothesup.shop.domain.Shop;
import semothon.team4.clothesup.user.domain.User;

public interface SavedShopRepository extends JpaRepository<SavedShop, Long> {
    List<SavedShop> findAllByUserOrderByCreatedAtDesc(User user);
    Optional<SavedShop> findByUserAndShop(User user, Shop shop);
    boolean existsByUserAndShop(User user, Shop shop);
    long countByUser(User user);
}
