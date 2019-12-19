package org.sep.sellerservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.sep.sellerservice.api.SellerRegistrationApi;
import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.service.SellerRegistrationService;
import org.sep.sellerservice.service.SellerRegistrationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SellerRegistrationController implements SellerRegistrationApi {

    private static final String HTTP_PREFIX = "http://";
    @Value("${server.address}")
    private String SERVER_ADDRESS;
    @Value("${server.port}")
    private String SERVER_PORT;
    private SellerRegistrationService sellerRegistrationService;

    @Autowired
    public SellerRegistrationController(SellerRegistrationService sellerRegistrationService) {
        this.sellerRegistrationService = sellerRegistrationService;
    }

    @Override
    public String registerSeller(SellerRegistrationRequest sellerRegistrationRequest) {
        SellerDto seller = this.sellerRegistrationService.save(sellerRegistrationRequest);
        return "redirect:" + HTTP_PREFIX + SERVER_ADDRESS + ":" + SERVER_PORT + "/choose_method?id=" + seller.getId();
    }
}