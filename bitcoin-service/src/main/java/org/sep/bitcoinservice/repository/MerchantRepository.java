package org.sep.bitcoinservice.repository;

import org.sep.bitcoinservice.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Merchant findByIssn(String issn);
}
