package org.sep.sellerservice.api;

import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.model.Seller;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@FeignClient("seller-service")
public interface SellerRegistrationApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    String registerSeller(@RequestBody SellerRegistrationRequest sellerRegistrationRequest);
}