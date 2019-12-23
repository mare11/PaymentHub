package org.sep.bitcoinservice.service;

import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodResponse;

public interface BitcoinService {
    PaymentMethodResponse createOrder(PaymentMethodRequest request);
    void completePayment(PaymentCompleteRequest request);
}
