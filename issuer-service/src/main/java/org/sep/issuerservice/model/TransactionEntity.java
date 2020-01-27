package org.sep.issuerservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.sep.pccservice.api.TransactionStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "transaction")
public class TransactionEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(precision = 10, scale = 2, nullable = false)
    private Double amount;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String acquirerOrderId;

    @Column
    private LocalDateTime acquirerTimestamp;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column
    private String description;

    @ManyToOne
    private CardEntity card;

}
