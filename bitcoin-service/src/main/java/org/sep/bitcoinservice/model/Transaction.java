package org.sep.bitcoinservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Column(nullable = false)
    private Long orderId;
    @Column(nullable = false)
    private TransactionStatus status;
    @Column(nullable = false)
    private String priceCurrency;
    @Column(nullable = false)
    private String price;
    @Column
    private String timestamp;
    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;
}
