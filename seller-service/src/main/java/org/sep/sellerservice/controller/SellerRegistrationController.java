package org.sep.sellerservice.controller;

import org.sep.sellerservice.api.SellerRegistrationApi;
import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.api.SellerRegistrationResponse;
import org.sep.sellerservice.dto.ChosenPaymentMethodsDto;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SellerRegistrationController implements SellerRegistrationApi {

    private static final String HTTP_PREFIX = "http://";
    @Value("${server.address}")
    private String SERVER_ADDRESS;
    @Value("${server.port}")
    private String SERVER_PORT;
    private final SellerService sellerService;

    @Autowired
    public SellerRegistrationController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public ResponseEntity<SellerRegistrationResponse> registerSeller(SellerRegistrationRequest sellerRegistrationRequest) {
        try {
            SellerDto seller = this.sellerService.save(sellerRegistrationRequest);
            SellerRegistrationResponse sellerRegistrationResponse = SellerRegistrationResponse.builder()
                    .redirectionUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/choose_method?id=" + seller.getId())
                    .build();
            return ResponseEntity.ok(sellerRegistrationResponse);
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/methods_chosen")
    public ResponseEntity chooseMethods(@RequestBody ChosenPaymentMethodsDto chosenPaymentMethodsDtos) {
        SellerDto sellerDto = this.sellerService.addPaymentMethods(chosenPaymentMethodsDtos);
        //todo call gateway service and send him selected payment methods
        return ResponseEntity.ok().build();
    }
}