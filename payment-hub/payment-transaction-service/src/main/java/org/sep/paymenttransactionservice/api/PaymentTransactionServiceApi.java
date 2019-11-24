package org.sep.paymenttransactionservice.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("transaction-service")
public interface PaymentTransactionServiceApi {

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    PaymentResponse createPayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/execute", consumes = MediaType.APPLICATION_JSON_VALUE)
    boolean executePayment(@RequestBody PaymentResponse paymentResponse);
}
