package org.sep.paypalservice.repository;

import org.sep.paymentgatewayservice.method.api.SubscriptionStatus;
import org.sep.paypalservice.model.SubscriptionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionTransactionRepository extends JpaRepository<SubscriptionTransaction, Long> {

    SubscriptionTransaction findBySubscriptionId(String subscriptionId);

    List<SubscriptionTransaction> findAllByStatus(SubscriptionStatus subscriptionStatus);

    SubscriptionTransaction findByMerchantSubscriptionId(String merchantSubscriptionId);
}