package org.sep.paymentgatewayservice.controller;

import org.sep.sellerservice.api.SellerRegistrationApi;
import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.model.Seller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@org.springframework.web.bind.annotation.RestController
public class SellerController {

    private SellerRegistrationApi sellerRegistrationApi;

    @Autowired
    public SellerController(SellerRegistrationApi sellerRegistrationApi) {
        this.sellerRegistrationApi = sellerRegistrationApi;
    }

    @PostMapping(value="/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String registerSeller(@RequestBody SellerRegistrationRequest sellerRegistrationRequest) {
        return this.sellerRegistrationApi.registerSeller(sellerRegistrationRequest);
    }
}
