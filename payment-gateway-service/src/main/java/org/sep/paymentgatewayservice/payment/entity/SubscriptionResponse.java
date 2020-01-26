package org.sep.paymentgatewayservice.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {
    private String subscriptionId;
    private String redirectionUrl;
    private CreateSubscriptionStatus status;
}