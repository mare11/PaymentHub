package org.sep.bitcoinservice.service;

import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;

public interface BitcoinService {
    PaymentResponse createOrder(PaymentRequest request);
    String completePayment(PaymentCompleteRequest request);
}
