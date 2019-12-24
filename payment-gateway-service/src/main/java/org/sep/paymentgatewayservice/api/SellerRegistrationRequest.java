package org.sep.paymentgatewayservice.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerRegistrationRequest {

    private String name;
    private String issn;
    private String returnUrl;
}