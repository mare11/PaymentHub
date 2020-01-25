package org.sep.paypalservice.service;

import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.dto.RegistrationDto;
import org.sep.paypalservice.model.PaymentTransaction;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest paymentMethodRequest);

    PaymentTransaction updateTransaction(PaymentTransaction paymentTransaction);

    PaymentTransaction findByOrderId(String orderId);

    String retrieveSellerRegistrationUrl(String merchantId);

    String registerSeller(RegistrationDto registrationDto);

    void checkUnfinishedTransactions();
}
