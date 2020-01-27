package org.sep.paymentgatewayservice.method.api;

import org.sep.paymentgatewayservice.payment.entity.FeignConfiguration;
import org.sep.paymentgatewayservice.payment.entity.NotifyPaymentMethodRegistrationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "payment-gateway-service", configuration = FeignConfiguration.class)
public interface PaymentMethodRegistrationApi {

    @PostMapping(value = "/payment_method_registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> registerPaymentMethod(@RequestBody PaymentMethodData paymentMethodData);

    @PostMapping(value = "/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Boolean> notifyMerchantIsRegistered(@RequestBody NotifyPaymentMethodRegistrationDto notifyPaymentMethodRegistrationDto);
}