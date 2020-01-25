package org.sep.paymentgatewayservice.seller.api;

import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.api.SellerRequest;
import org.sep.paymentgatewayservice.payment.entity.FeignConfiguration;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "seller-service/api", configuration = FeignConfiguration.class)
public interface SellerServiceApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RedirectionResponse> registerSeller(@RequestBody SellerRequest sellerRequest);

    @PostMapping(value = "/prepare_payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentResponse> preparePayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/enable", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> enableSeller(@RequestBody String sellerIssn);

    @PostMapping(value = "/subscription/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RedirectionResponse> prepareSubscription(@RequestBody SellerRequest sellerRequest);
}