package org.sep.bankservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {
    private String merchantId;
    private String bankMerchantId;
    private String bankMerchantPassword;
}
