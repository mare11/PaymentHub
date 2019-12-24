package org.sep.bitcoinservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CGResponse {
    private Long id;
    private String status;
    private String price_currency;
    private String price_amount;
    private String receive_currency;
    private String receive_amount;
    private String created_at;
    private Long order_id;
    private String payment_url;
    private String token;
}
