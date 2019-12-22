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
    private String orderId;
    private Long sellerIssn;
    private String priceAmount;
    private String priceCurrency;
    private String title;
    private String description;
    private String cancelUrl;
    private String errorUrl;
    private String successUrl;
}
