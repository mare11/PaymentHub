package org.sep.paymenttransactionservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentdataservice.api.PaymentDataServiceApi;
import org.sep.paymentdataservice.api.PaymentMethod;
import org.sep.paymenttransactionservice.api.PaymentMethodApi;
import org.sep.paymenttransactionservice.api.PaymentRequest;
import org.sep.paymenttransactionservice.api.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

@Slf4j
@Service
public class PaymentTransactionService {

    private final PaymentDataServiceApi paymentDataServiceApi;
    private final PaymentMethodApi paymentMethodApi;

    @Autowired
    public PaymentTransactionService(final PaymentDataServiceApi paymentDataServiceApi, final PaymentMethodApi paymentMethodApi) {
        this.paymentDataServiceApi = paymentDataServiceApi;
        this.paymentMethodApi = paymentMethodApi;
    }

    public PaymentResponse createPayment(final PaymentRequest paymentRequest) {
        final Optional<PaymentMethod> optionalPaymentMethod = paymentDataServiceApi.getPaymentMethodByName(paymentRequest.getMethod());
        if (optionalPaymentMethod.isPresent()) {
            final PaymentMethod paymentMethod = optionalPaymentMethod.get();
            log.info("Payment method found with name: {} and address: {}", paymentMethod.getName(), paymentMethod.getAddress());
            return paymentMethodApi.createPayment(URI.create(paymentMethod.getAddress()), paymentRequest);
        }
        log.error("Payment method for name: {} not found!", paymentRequest.getMethod());
        return null;
    }

    public boolean executePayment(final PaymentResponse paymentResponse) {
        final Optional<PaymentMethod> optionalPaymentMethod = paymentDataServiceApi.getPaymentMethodByName(paymentResponse.getMethod());
        if (optionalPaymentMethod.isPresent()) {
            final PaymentMethod paymentMethod = optionalPaymentMethod.get();
            return paymentMethodApi.executePayment(URI.create(paymentMethod.getAddress()), paymentResponse);
        }
        log.error("Payment method for name: {} not found!", paymentResponse.getMethod());
        return false;
    }
}
