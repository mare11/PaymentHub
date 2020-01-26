package org.sep.bitcoinservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sep.paymentgatewayservice.method.api.MerchantOrderStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String orderId;
    @Column(unique = true, nullable = false)
    private Long cgId;
    @Column
    private String item;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MerchantOrderStatus status;
    @Column(nullable = false)
    private String priceCurrency;
    @Column(nullable = false)
    private String price;
    @Column
    private String timestamp;
    @Column(nullable = false)
    private String returnUrl;
    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;
}
