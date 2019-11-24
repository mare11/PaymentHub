package org.sep.paymentclientservice.api;

import org.sep.paymenttransactionservice.api.PaymentRequest;
import org.sep.paymenttransactionservice.api.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "client-service")
public interface PaymentClientServiceApi {

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    PaymentResponse createPayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/execute", consumes = MediaType.APPLICATION_JSON_VALUE)
    boolean executePayment(@RequestBody PaymentResponse paymentResponse);

}
