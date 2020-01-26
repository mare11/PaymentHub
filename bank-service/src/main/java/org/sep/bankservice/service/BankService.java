package org.sep.bankservice.service;

import org.sep.bankservice.model.Merchant;
import org.sep.paymentgatewayservice.method.api.MerchantOrderStatus;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;

public interface BankService {

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    String completePayment(PaymentCompleteRequest paymentCompleteRequest);

    String retrieveMerchantRegistrationUrl(String merchantId);

    String registerMerchant(Merchant merchant);

    MerchantOrderStatus getOrderStatus(String orderId);
}
