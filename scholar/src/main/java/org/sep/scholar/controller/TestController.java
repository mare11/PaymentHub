package org.sep.scholar.controller;

import org.sep.scholar.controller.model.PaymentRequest;
import org.sep.scholar.controller.model.PaymentResponse;
import org.sep.scholar.controller.model.SellerRegistrationRequest;
import org.sep.scholar.controller.model.SellerRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController {

    private static final String HTTP_PREFIX = "http://";
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final String SERVER_PORT = "8082";
    private final RestTemplate restTemplate;

    @Autowired
    public TestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SellerRegistrationResponse> registerSeller(@RequestBody SellerRegistrationRequest sellerRegistrationRequest) {
        return this.restTemplate.postForEntity(HTTP_PREFIX + SERVER_ADDRESS + ":" + SERVER_PORT + "/register", sellerRegistrationRequest, SellerRegistrationResponse.class);
    }

    @PostMapping(value = "/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResponse> preparePayment(@RequestBody PaymentRequest paymentRequest) {
        paymentRequest.setSellerIssn(12345678L);
        return this.restTemplate.postForEntity(HTTP_PREFIX + SERVER_ADDRESS + ":" + SERVER_PORT + "/prepare", paymentRequest, PaymentResponse.class);
    }
}