package semothon.team4.clothesup.shop.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.shop.domain.Shop;
import semothon.team4.clothesup.shop.domain.ShopPrice;

public interface ShopPriceRepository extends JpaRepository<ShopPrice, Long> {
    List<ShopPrice> findByShop(Shop shop);
}
