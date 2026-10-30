package com.pucsp.alexandria.adapter.out.persistence.jpa;
import com.pucsp.alexandria.adapter.out.persistence.entity.RecommendationItemEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.RecommendationItemId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RecommendationItemJpaRepository extends JpaRepository<RecommendationItemEntity,RecommendationItemId> {
  List<RecommendationItemEntity> findByIdBatchIdOrderByIdPosition(Long batchId);
}
