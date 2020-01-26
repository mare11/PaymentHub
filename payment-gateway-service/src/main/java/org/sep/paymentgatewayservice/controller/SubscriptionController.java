package org.sep.paymentgatewayservice.controller;

import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.seller.api.MerchantServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {

    private final MerchantServiceApi merchantServiceApi;

    @Autowired
    public SubscriptionController(final MerchantServiceApi merchantServiceApi) {
        this.merchantServiceApi = merchantServiceApi;
    }

    @PostMapping(value = "/subscription", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedirectionResponse> subscribe(@RequestBody final MerchantRequest merchantRequest) {
        return this.merchantServiceApi.prepareSubscription(merchantRequest);
    }
}