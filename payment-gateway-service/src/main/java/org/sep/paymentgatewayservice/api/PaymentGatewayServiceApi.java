package org.sep.paymentgatewayservice.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("payment-gateway")
public interface PaymentGatewayServiceApi {

    @PostMapping(value = "/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    PaymentResponse preparePayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    PaymentResponse createPayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/execute", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    PaymentResponse executePayment(@RequestBody PaymentResponse paymentResponse);
}
