package org.sep.paymentgatewayservice.controller;

import org.sep.paymentgatewayservice.api.SellerRegistrationRequest;
import org.sep.paymentgatewayservice.api.SellerRegistrationResponse;
import org.sep.sellerservice.api.SellerServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SellerController {

    private final SellerServiceApi sellerServiceApi;

    @Autowired
    public SellerController(SellerServiceApi sellerServiceApi) {
        this.sellerServiceApi = sellerServiceApi;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SellerRegistrationResponse> registerSeller(@RequestBody SellerRegistrationRequest sellerRegistrationRequest) {
        return this.sellerServiceApi.registerSeller(sellerRegistrationRequest);
    }
}