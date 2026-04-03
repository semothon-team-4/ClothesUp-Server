package semothon.team4.clothesup.analysis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.analysis.domain.ConditionAnalysis;

public interface ConditionAnalysisRepository extends JpaRepository<ConditionAnalysis, Long> {
    Optional<ConditionAnalysis> findByAnalysis(Analysis analysis);
    List<ConditionAnalysis> findByAnalysisIn(List<Analysis> analyses);
}
