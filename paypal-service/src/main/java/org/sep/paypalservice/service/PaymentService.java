package org.sep.paypalservice.service;

import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodResponse;
import org.sep.paypalservice.model.PaymentTransaction;

public interface PaymentService {

    PaymentMethodResponse createPayment(PaymentMethodRequest paymentMethodRequest);

    void completePayment(PaymentCompleteRequest paymentCompleteRequest);

    PaymentTransaction update(PaymentTransaction paymentTransaction);
}
