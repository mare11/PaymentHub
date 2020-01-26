package org.sep.paymentgatewayservice.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private String merchantId;
    private String merchantName;
    private String merchantOrderId;
    private String item;
    private Double price;
    private String description;
    private String method;
    private String returnUrl;
}