package org.sep.sellerservice.service;

import org.sep.paymentgatewayservice.api.PaymentRequest;
import org.sep.paymentgatewayservice.api.PaymentResponse;
import org.sep.sellerservice.dto.CustomerPaymentDto;
import org.sep.sellerservice.dto.PaymentDto;
import org.sep.sellerservice.model.Payment;

public interface PaymentService {

    Payment findById(Long id);

    PaymentDto save(PaymentRequest paymentRequest);

    PaymentResponse proceedPayment(CustomerPaymentDto customerPaymentDto);
}