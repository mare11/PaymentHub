package org.sep.acquirerservice.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
    private String merchantId;
    private String merchantPassword;
    private Long merchantOrderId;
    private LocalDateTime merchantTimestamp;
    private Double amount;
    private String successUrl;
    private String errorUrl;
}
