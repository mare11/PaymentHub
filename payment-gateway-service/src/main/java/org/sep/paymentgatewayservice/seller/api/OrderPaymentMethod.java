package org.sep.paymentgatewayservice.seller.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaymentMethod {

    private PaymentMethod paymentMethod;
    private Boolean orderExpired;
}