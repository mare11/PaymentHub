package org.sep.paymentgatewayservice.service;

import org.sep.paymentgatewayservice.methodapi.PaymentMethodData;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;

public interface PaymentGatewayService {

    PaymentResponse preparePayment(PaymentRequest paymentRequest);

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    void registerPaymentMethod(PaymentMethodData paymentMethodData);
}
