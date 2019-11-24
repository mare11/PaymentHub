package org.sep.paymenttransactionservice.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private String item;
    private Integer amount;
    private Double price;
    private String method;
    private String token;
    private String returnUrl;
}
