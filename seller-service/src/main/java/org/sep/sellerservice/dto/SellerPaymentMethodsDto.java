package org.sep.sellerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerPaymentMethodsDto {

    private Long sellerId;
    private List<PaymentMethod> paymentMethods;
}