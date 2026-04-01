package semothon.team4.clothesup.analysis.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.analysis.domain.CareLabel;
import semothon.team4.clothesup.analysis.domain.CareLabelAnalysis;

public interface CareLabelRepository extends JpaRepository<CareLabel, Long> {
    List<CareLabel> findByCareLabelAnalysis(CareLabelAnalysis careLabelAnalysis);
}
