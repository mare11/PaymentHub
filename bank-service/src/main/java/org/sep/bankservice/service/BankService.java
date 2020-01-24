package org.sep.bankservice.service;

import org.sep.bankservice.model.Merchant;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;

public interface BankService {

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    String completePayment(PaymentCompleteRequest paymentCompleteRequest);

    String retrieveSellerRegistrationUrl(String issn);

    String registerSeller(Merchant merchant);
}
