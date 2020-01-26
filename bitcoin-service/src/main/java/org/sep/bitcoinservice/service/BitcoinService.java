package org.sep.bitcoinservice.service;

import org.sep.bitcoinservice.model.Merchant;
import org.sep.paymentgatewayservice.method.api.MerchantOrderStatus;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;

public interface BitcoinService {
    PaymentResponse createOrder(PaymentRequest request);

    String completePayment(PaymentCompleteRequest request);

    MerchantOrderStatus getOrderStatus(String orderId);

    String retrieveMerchantRegistrationUrl(String merchantId);

    String registerMerchant(Merchant merchant);
}
