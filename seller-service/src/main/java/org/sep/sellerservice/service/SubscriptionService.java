package org.sep.sellerservice.service;

import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionResponse;
import org.sep.sellerservice.dto.CustomerSubscriptionDto;
import org.sep.sellerservice.model.Subscription;

import java.util.List;

public interface SubscriptionService {

    Subscription findById(String id);

    String prepareSubscription(MerchantRequest merchantRequest);

    List<SubscriptionPlan> retrieveSubscriptionPlans(String id);

    SubscriptionResponse createSubscription(CustomerSubscriptionDto customerSubscriptionDto);
}