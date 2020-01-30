package org.sep.paypalservice.service;

import org.sep.paymentgatewayservice.method.api.SubscriptionStatus;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paypalservice.dto.CompleteDto;
import org.sep.paypalservice.dto.RedirectionDto;
import org.sep.paypalservice.model.SubscriptionTransaction;

import java.util.List;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(SubscriptionRequest subscriptionRequest);

    List<SubscriptionPlan> retrieveSubscriptionPlans(String merchantId);

    SubscriptionTransaction findBySubscriptionId(String subscriptionId);

    void updateTransaction(SubscriptionTransaction subscriptionTransaction);

    RedirectionDto completeSubscriptionTransaction(CompleteDto completeDto);

    SubscriptionCancelResponse cancelSubscription(SubscriptionCancelRequest subscriptionCancelRequest);

    SubscriptionStatus getSubscriptionStatus(final String subscriptionId);

    void checkUnfinishedTransactions();
}