package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.jpa.SubscriptionJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.SubscriptionMapper;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.SubscriptionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

  private final SubscriptionJpaRepository jpaRepository;
  private final SubscriptionMapper mapper;

  public SubscriptionRepositoryImpl(
      SubscriptionJpaRepository jpaRepository,
      SubscriptionMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Subscription save(Subscription subscription) {
    var entity = mapper.toPersistence(subscription);
    var saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Subscription> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Subscription> findByUserId(Long userId) {
    return jpaRepository.findByUserId(userId).map(mapper::toDomain);
  }

  @Override
  public Optional<Subscription> findByMpPaymentId(Long mpPaymentId) {
    return jpaRepository.findByMpPaymentId(mpPaymentId).map(mapper::toDomain);
  }

  @Override
  public List<Subscription> findTrialsEndingBefore(LocalDateTime now) {
    return jpaRepository
        .findByStatusAndTrialEndsAtBefore(SubscriptionStatus.TRIALING, now)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<Subscription> findActiveEndingBefore(LocalDateTime now) {
    return jpaRepository
        .findByStatusAndCurrentPeriodEndsAtBefore(SubscriptionStatus.ACTIVE, now)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }
}
