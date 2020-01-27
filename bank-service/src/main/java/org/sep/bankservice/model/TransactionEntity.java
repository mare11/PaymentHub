package org.sep.bankservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transaction")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String bankTransactionId;

    @Column(nullable = false)
    private String item;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MerchantOrderStatus status;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String returnUrl;

    @ManyToOne
    private MerchantEntity merchant;
}
