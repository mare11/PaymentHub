package org.sep.bitcoinservice.service;

import org.sep.bitcoinservice.model.Merchant;

public interface MerchantService {

    Merchant findByIssn(String issn);
    Merchant save(Merchant merchant);
}
