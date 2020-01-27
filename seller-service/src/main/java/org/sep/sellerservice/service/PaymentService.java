package org.sep.sellerservice.service;

import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.sellerservice.dto.CustomerPaymentDto;
import org.sep.sellerservice.model.Payment;

public interface PaymentService {

    Payment findById(String id);

    String preparePayment(PaymentRequest paymentRequest);

    PaymentResponse proceedPayment(CustomerPaymentDto customerPaymentDto);

    PaymentMethod getOrderPaymentMethod(String orderId);
}