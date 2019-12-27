package org.sep.paymentgatewayservice.methodapi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "payment-gateway-service")
public interface PaymentMethodRegistrationApi {

    @PostMapping(value = "/payment_method_registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> registerPaymentMethod(@RequestBody PaymentMethodData paymentMethodData);

    @PostMapping(value = "/proceed", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> proceedToNextPaymentMethod(@RequestBody String merchantId);
}