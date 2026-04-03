package semothon.team4.clothesup.analysis.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.analysis.domain.CareLabelList;

public interface CareLabelListRepository extends JpaRepository<CareLabelList, Long> {
    Optional<CareLabelList> findByName(String name);
}
