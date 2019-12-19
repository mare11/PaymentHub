package org.sep.scholar.controller;

import org.sep.scholar.controller.model.SellerRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    private static final String HTTP_PREFIX = "http://";
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final String SERVER_PORT = "8082";
    private RestTemplate restTemplate;

    @Autowired
    public RestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String registerSeller(@RequestBody SellerRegistrationRequest sellerRegistrationRequest) {
        return restTemplate.postForObject(HTTP_PREFIX + SERVER_ADDRESS + ":" + SERVER_PORT + "/register", sellerRegistrationRequest, String.class);
    }
}