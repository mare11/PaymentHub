package org.sep.paymentgatewayservice.service;

import org.sep.paymentgatewayservice.api.PaymentRequest;
import org.sep.paymentgatewayservice.api.PaymentResponse;

public interface PaymentGatewayService {

    PaymentResponse preparePayment(PaymentRequest paymentRequest);

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    PaymentResponse executePayment(PaymentResponse paymentResponse);
}
