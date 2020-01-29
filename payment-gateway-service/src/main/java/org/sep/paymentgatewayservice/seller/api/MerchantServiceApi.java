package org.sep.paymentgatewayservice.seller.api;

import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.payment.entity.FeignConfiguration;
import org.sep.paymentgatewayservice.payment.entity.NotifyPaymentMethodRegistrationDto;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "seller-service/api", configuration = FeignConfiguration.class)
public interface MerchantServiceApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RedirectionResponse> registerMerchant(@RequestBody MerchantRequest merchantRequest);

    @PostMapping(value = "/prepare_payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RedirectionResponse> preparePayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/subscription/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RedirectionResponse> prepareSubscription(@RequestBody MerchantRequest merchantRequest);

    @GetMapping(value = "/payment_method/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OrderPaymentMethod> getOrderPaymentMethod(@PathVariable String orderId);

    @PostMapping(value = "/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Boolean> notifyMerchantIsRegistered(@RequestBody NotifyPaymentMethodRegistrationDto notifyPaymentMethodRegistrationDto);
}