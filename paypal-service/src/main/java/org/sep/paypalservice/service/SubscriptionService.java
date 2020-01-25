package org.sep.paypalservice.service;

import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionResponse;
import org.sep.paypalservice.model.SubscriptionTransaction;

import java.util.List;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(SubscriptionRequest subscriptionRequest);

    List<SubscriptionPlan> retrieveSubscriptionPlans(String merchantId);

    SubscriptionTransaction findBySubscriptionId(String subscriptionId);

    SubscriptionTransaction updateTransaction(SubscriptionTransaction subscriptionTransaction);

    void checkUnfinishedTransactions();
}