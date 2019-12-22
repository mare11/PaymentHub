package org.sep.paymentgatewayservice.methodapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponse {
    private String orderId;
    private String paymentUrl;
}
