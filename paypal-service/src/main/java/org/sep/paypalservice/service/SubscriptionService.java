package org.sep.paypalservice.service;

import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionResponse;
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

    void checkUnfinishedTransactions();
}