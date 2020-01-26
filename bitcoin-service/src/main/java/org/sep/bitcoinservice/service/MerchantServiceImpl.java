package org.sep.bitcoinservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.bitcoinservice.exceptions.NoMerchantFoundException;
import org.sep.bitcoinservice.model.Merchant;
import org.sep.bitcoinservice.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
public class MerchantServiceImpl implements MerchantService {

    private MerchantRepository merchantRepository;

    @Autowired
    public MerchantServiceImpl(MerchantRepository merchantRepository){
        this.merchantRepository = merchantRepository;
    }

    @Override
    public Merchant findByMerchantId(String merchantId) {
        Merchant merchant = this.merchantRepository.findByMerchantId(merchantId);
        if (merchant != null) {
            return merchant;
        }else{
            log.error("Merchant with id: {} not found", merchantId);
            throw new NoMerchantFoundException(merchantId);
        }
    }

    @Override
    public Merchant save(Merchant merchant) {
        if (Stream.of(merchant, merchant.getToken(), merchant.getMerchantId())
                .anyMatch(Objects::isNull)) {
            return null;
        }
        return this.merchantRepository.save(merchant);
    }
}
