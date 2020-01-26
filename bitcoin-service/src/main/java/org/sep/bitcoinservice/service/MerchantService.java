package org.sep.bitcoinservice.service;

import org.sep.bitcoinservice.model.Merchant;

public interface MerchantService {
    Merchant findByMerchantId(String merchantId);
    Merchant save(Merchant merchant);
}
