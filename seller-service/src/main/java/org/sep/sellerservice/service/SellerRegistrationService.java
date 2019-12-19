package org.sep.sellerservice.service;

import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.dto.SellerDto;

public interface SellerRegistrationService {
    SellerDto save(SellerRegistrationRequest sellerRegistrationRequest);
}
