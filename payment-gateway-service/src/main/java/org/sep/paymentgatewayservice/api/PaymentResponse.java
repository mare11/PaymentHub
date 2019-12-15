package org.sep.paymentgatewayservice.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String paymentId;
    private String payerId;
    private String method;
    private String token;
    private Boolean outcome;
    private String redirectUrl;
}
