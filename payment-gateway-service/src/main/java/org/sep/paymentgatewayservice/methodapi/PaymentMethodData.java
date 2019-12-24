package org.sep.paymentgatewayservice.methodapi;

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
    private String serviceName;
    private Integer port;
}