package org.sep.paymentgatewayservice.controller;

import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.api.SellerRequest;
import org.sep.paymentgatewayservice.seller.api.SellerServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {

    private final SellerServiceApi sellerServiceApi;

    @Autowired
    public SubscriptionController(final SellerServiceApi sellerServiceApi) {
        this.sellerServiceApi = sellerServiceApi;
    }

    @PostMapping(value = "/subscription", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedirectionResponse> subscribe(@RequestBody final SellerRequest sellerRequest) {
        return this.sellerServiceApi.prepareSubscription(sellerRequest);
    }
}