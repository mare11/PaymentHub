package org.sep.paymentgatewayservice.methodapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodRequest {
    private Long sellerIssn;
    private String sellerName;
    private String item;
    private Double price;
    private String priceCurrency;
    private String description;
    private String cancelUrl;
    private String successUrl;
}