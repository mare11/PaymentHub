package org.sep.pccservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
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
    private String acquirerOrderId;

    @Column(nullable = false)
    private LocalDateTime acquirerTimestamp;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String pan;

    @Column(nullable = false)
    private String ccv;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private String cardholderName;
}
