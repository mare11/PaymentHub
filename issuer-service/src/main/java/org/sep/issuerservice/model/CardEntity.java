package org.sep.issuerservice.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "card")
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pan;

    @Column(nullable = false)
    private String ccv;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private Double availableAmount;

    @Column(nullable = false)
    private Double reservedAmount;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private String merchantPassword;

    @ManyToOne
    @ToString.Exclude
    private ClientEntity client;
}
