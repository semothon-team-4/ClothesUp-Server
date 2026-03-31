package semothon.team4.clothesup.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
