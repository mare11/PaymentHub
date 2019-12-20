package org.sep.paymentgatewayservice.controller;

import org.sep.sellerservice.api.SellerRegistrationApi;
import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.api.SellerRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SellerController {

    private final SellerRegistrationApi sellerRegistrationApi;

    @Autowired
    public SellerController(SellerRegistrationApi sellerRegistrationApi) {
        this.sellerRegistrationApi = sellerRegistrationApi;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SellerRegistrationResponse> registerSeller(@RequestBody SellerRegistrationRequest sellerRegistrationRequest) {
        return this.sellerRegistrationApi.registerSeller(sellerRegistrationRequest);
    }
}