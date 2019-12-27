package org.sep.acquirerservice.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "transaction")
public class TransactionEntity {

    @Id
    @Type(type = "org.hibernate.type.UUIDCharType")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column
    private String item;

    @Column(precision = 10, scale = 2, nullable = false)
    private Double amount;

    @Column
    private String description;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String successUrl;

    @Column(nullable = false)
    private String errorUrl;

    @ManyToOne
    @ToString.Exclude
    private CardEntity card;

}
