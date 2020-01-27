package org.sep.paymentgatewayservice.service;

import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.method.api.PaymentMethodData;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paymentgatewayservice.seller.api.MerchantPaymentMethods;

import java.util.List;

public interface PaymentGatewayService {

    RedirectionResponse preparePayment(PaymentRequest paymentRequest);

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    void registerPaymentMethod(PaymentMethodData paymentMethodData);

    String registerMerchantInPaymentMethod(MerchantPaymentMethods merchantPaymentMethods);

    String proceedToNextPaymentMethod(String merchantId);

    List<SubscriptionPlan> retrieveSubscriptionPlans(String merchantId);

    SubscriptionResponse createSubscription(SubscriptionRequest subscriptionRequest);

    SubscriptionCancelResponse cancelSubscription(SubscriptionCancelRequest subscriptionCancelRequest);
}
