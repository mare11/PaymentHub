package org.sep.sellerservice.service;

import org.sep.paymentgatewayservice.api.SellerRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionResponse;
import org.sep.sellerservice.dto.CustomerSubscriptionDto;
import org.sep.sellerservice.model.Subscription;

import java.util.List;

public interface SubscriptionService {

    Subscription findById(Long id);

    Long prepareSubscription(SellerRequest sellerRequest);

    List<SubscriptionPlan> retrieveSubscriptionPlans(Long id);

    SubscriptionResponse createSubscription(CustomerSubscriptionDto customerSubscriptionDto);
}