package org.sep.paymentgatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.api.PaymentRequest;
import org.sep.paymentgatewayservice.api.PaymentResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    @Override
    public PaymentResponse preparePayment(PaymentRequest paymentRequest) {
        // todo call the seller service api for redirect url
        return null;
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        // todo call the selected payment method api and redirect user to fill in his details
        return null;
    }

    @Override
    public PaymentResponse executePayment(PaymentResponse paymentResponse) {
        //callback from payment method service for saving transactional data
        // todo call seller service api for displaying outcome and redirect link to the original client frontend
        return null;
    }
}
