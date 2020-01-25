package org.sep.paymentgatewayservice.seller.api;

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

    private String sellerIssn;
    private String returnUrl;
    private List<PaymentMethod> paymentMethods;
}