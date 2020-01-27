package org.sep.paypalservice.service;

import org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.dto.CompleteDto;
import org.sep.paypalservice.dto.RedirectionDto;
import org.sep.paypalservice.model.PaymentTransaction;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest paymentMethodRequest);

    void updateTransaction(PaymentTransaction paymentTransaction);

    PaymentTransaction findByOrderId(String orderId);

    RedirectionDto completePaymentTransaction(CompleteDto completeDto);

    MerchantOrderStatus getOrderStatus(String merchantOrderId);

    void checkUnfinishedTransactions();
}
