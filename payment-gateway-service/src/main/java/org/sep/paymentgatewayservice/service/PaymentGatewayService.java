package org.sep.paymentgatewayservice.service;

import org.sep.paymentgatewayservice.methodapi.PaymentMethodData;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.sellerservice.api.SellerPaymentMethods;

public interface PaymentGatewayService {

    PaymentResponse preparePayment(PaymentRequest paymentRequest);

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    void registerPaymentMethod(PaymentMethodData paymentMethodData);

    String registerSellerInPaymentMethod(SellerPaymentMethods sellerPaymentMethods);

    String proceedToNextPaymentMethod(String sellerIssn);
}
