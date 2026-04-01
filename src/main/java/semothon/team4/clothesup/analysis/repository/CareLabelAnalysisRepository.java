package semothon.team4.clothesup.analysis.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.analysis.domain.CareLabelAnalysis;

public interface CareLabelAnalysisRepository extends JpaRepository<CareLabelAnalysis, Long> {
    Optional<CareLabelAnalysis> findByAnalysis(Analysis analysis);
}
