package com.pucsp.alexandria.domain.subscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {

  Subscription save(Subscription subscription);

  Optional<Subscription> findById(Long id);

  Optional<Subscription> findByUserId(Long userId);

  Optional<Subscription> findByMpPaymentId(Long mpPaymentId);

  List<Subscription> findTrialsEndingBefore(LocalDateTime now);

  List<Subscription> findActiveEndingBefore(LocalDateTime now);
}
