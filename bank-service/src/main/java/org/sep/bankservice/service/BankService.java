package org.sep.bankservice.service;

import org.sep.bankservice.model.Merchant;
import org.sep.bankservice.model.TransactionResponse;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;

public interface BankService {

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    String completePayment(PaymentCompleteRequest paymentCompleteRequest);

    String retrieveMerchantRegistrationUrl(String merchantId);

    TransactionResponse registerMerchant(Merchant merchant);

    MerchantOrderStatus getOrderStatus(String orderId);
}
