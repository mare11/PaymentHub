package org.sep.sellerservice.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerPaymentMethods {

    private Long sellerId;
    private List<PaymentMethod> paymentMethods;
}