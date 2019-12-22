package org.sep.paymentgatewayservice.methodapi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("payment-gateway-service/method")
public interface PaymentGatewayServiceMethodApi {

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentMethodResponse> createPayment(@RequestBody PaymentMethodRequest paymentMethodRequest);

    @PostMapping(value = "/complete", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity completePayment(@RequestBody PaymentCompleteRequest paymentCompleteRequest);
}
