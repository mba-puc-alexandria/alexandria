package com.pucsp.alexandria.adapter.out.persistence.jpa;

import com.pucsp.alexandria.adapter.out.persistence.entity.SubscriptionEntity;
import com.pucsp.alexandria.domain.subscription.SubscriptionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, Long> {

  Optional<SubscriptionEntity> findByUserId(Long userId);

  Optional<SubscriptionEntity> findByMpPaymentId(Long mpPaymentId);

  List<SubscriptionEntity> findByStatusAndTrialEndsAtBefore(
      SubscriptionStatus status, LocalDateTime now);

  List<SubscriptionEntity> findByStatusAndCurrentPeriodEndsAtBefore(
      SubscriptionStatus status, LocalDateTime now);
}
