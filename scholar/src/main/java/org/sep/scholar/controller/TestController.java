package org.sep.scholar.controller;

import org.sep.scholar.controller.model.PaymentRequest;
import org.sep.scholar.controller.model.PaymentResponse;
import org.sep.scholar.controller.model.RedirectionResponse;
import org.sep.scholar.controller.model.SellerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController {

    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    @Value("${ip.address}")
    private String SERVER_ADDRESS;
    @Value("${server.port}")
    private String SERVER_PORT;
    private static final String GATEWAY_PORT = "8082";
    private final RestTemplate restTemplate;

    @Autowired
    public TestController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedirectionResponse> registerSeller(@RequestBody final SellerRequest sellerRequest) {
        sellerRequest.setReturnUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT);
        return this.restTemplate.postForEntity(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + GATEWAY_PORT + "/register", sellerRequest, RedirectionResponse.class);
    }

    @PostMapping(value = "/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResponse> preparePayment(@RequestBody final PaymentRequest paymentRequest) {
        paymentRequest.setSellerIssn("12345678");
        paymentRequest.setReturnUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT);
        return this.restTemplate.postForEntity(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + GATEWAY_PORT + "/prepare", paymentRequest, PaymentResponse.class);
    }

    @PostMapping(value = "/subscription", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedirectionResponse> subscribe(@RequestBody final SellerRequest sellerRequest) {
        sellerRequest.setReturnUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT);
        return this.restTemplate.postForEntity(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + GATEWAY_PORT + "/subscription", sellerRequest, RedirectionResponse.class);
    }
}