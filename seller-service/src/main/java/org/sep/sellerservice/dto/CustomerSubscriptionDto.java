package org.sep.sellerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSubscriptionDto {

    private String merchantSubscriptionId;
    private SubscriptionPlan plan;
}