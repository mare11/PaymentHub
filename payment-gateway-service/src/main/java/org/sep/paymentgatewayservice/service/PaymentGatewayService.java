package org.sep.paymentgatewayservice.service;

import org.sep.paymentgatewayservice.method.api.PaymentMethodData;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paymentgatewayservice.seller.api.SellerPaymentMethods;

import java.util.List;

public interface PaymentGatewayService {

    PaymentResponse preparePayment(PaymentRequest paymentRequest);

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    void registerPaymentMethod(PaymentMethodData paymentMethodData);

    String registerSellerInPaymentMethod(SellerPaymentMethods sellerPaymentMethods);

    String proceedToNextPaymentMethod(String sellerIssn);

    List<SubscriptionPlan> retrieveSubscriptionPlans(String merchantId);

    SubscriptionResponse createSubscription(SubscriptionRequest subscriptionRequest);
}
