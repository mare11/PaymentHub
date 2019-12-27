package org.sep.bankservice.model;

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
    private String merchantOrderId;
    private LocalDateTime merchantTimestamp;
    private String item;
    private Double amount;
    private String description;
    private String successUrl;
    private String errorUrl;
}
