package semothon.team4.clothesup.analysis.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.user.domain.User;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findByUser(User user);
}
