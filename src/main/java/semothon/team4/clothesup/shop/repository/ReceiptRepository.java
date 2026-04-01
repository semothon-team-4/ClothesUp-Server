package semothon.team4.clothesup.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.shop.domain.Receipt;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}
