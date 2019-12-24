package org.sep.paypalservice.service;

import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.model.PaymentTransaction;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest paymentMethodRequest);

    String completePayment(PaymentCompleteRequest paymentCompleteRequest);

    PaymentTransaction update(PaymentTransaction paymentTransaction);
}
