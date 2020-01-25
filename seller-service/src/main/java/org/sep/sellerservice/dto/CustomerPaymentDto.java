package org.sep.sellerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPaymentDto {

    private Long paymentId;
    private PaymentMethod paymentMethod;
}