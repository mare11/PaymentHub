package org.sep.sellerservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class MerchantPaymentMethodId implements Serializable {

    private static final long serialVersionUID = -3769010745992261786L;

    @ManyToOne
    private Merchant merchant;

    @ManyToOne
    @JoinColumn(name = "method_id")
    private PaymentMethodEntity paymentMethodEntity;
}