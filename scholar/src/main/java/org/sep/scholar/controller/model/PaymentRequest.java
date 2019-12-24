package org.sep.scholar.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private String sellerIssn;
    private String sellerName;
    private String item;
    private Double price;
    private String priceCurrency;
    private String description;
    private String method;
    private String returnUrl;
}