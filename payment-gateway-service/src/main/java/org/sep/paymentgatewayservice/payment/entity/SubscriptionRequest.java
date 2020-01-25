package org.sep.paymentgatewayservice.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRequest {

    private String planId;
    private String merchantName;
    private String merchantId;
    private String returnUrl;
}