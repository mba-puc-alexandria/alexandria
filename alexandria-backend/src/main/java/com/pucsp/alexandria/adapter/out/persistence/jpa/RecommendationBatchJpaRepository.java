package com.pucsp.alexandria.adapter.out.persistence.jpa;
import com.pucsp.alexandria.adapter.out.persistence.entity.RecommendationBatchEntity;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RecommendationBatchJpaRepository extends JpaRepository<RecommendationBatchEntity,Long> {
  Optional<RecommendationBatchEntity> findByUserIdAndReferenceDate(Long userId, LocalDate referenceDate);
}
