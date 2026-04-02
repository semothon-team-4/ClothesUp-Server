package semothon.team4.clothesup.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.analysis.domain.Analysis;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
}
