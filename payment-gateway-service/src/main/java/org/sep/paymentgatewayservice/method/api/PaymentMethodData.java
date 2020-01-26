package org.sep.paymentgatewayservice.method.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodData {

    private String name;
    private String host;
    private Integer port;
}