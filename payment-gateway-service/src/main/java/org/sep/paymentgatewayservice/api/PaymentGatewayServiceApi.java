package org.sep.paymentgatewayservice.api;

import org.sep.paymentgatewayservice.payment.entity.FeignConfiguration;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.sellerservice.api.SellerPaymentMethods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "payment-gateway-service", configuration = FeignConfiguration.class)
public interface PaymentGatewayServiceApi {

    @PostMapping(value = "/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentResponse> preparePayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/payment_methods/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> registerPaymentMethods(@RequestBody SellerPaymentMethods sellerPaymentMethods);
}