package semothon.team4.clothesup.shop.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import semothon.team4.clothesup.user.domain.User;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "saved_shop")
public class SavedShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;

    private LocalDateTime createdAt;

    public SavedShop(User user, Shop shop) {
        this.user = user;
        this.shop = shop;
        this.createdAt = LocalDateTime.now();
    }
}
