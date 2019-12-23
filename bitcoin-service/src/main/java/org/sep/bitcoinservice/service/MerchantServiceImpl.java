package org.sep.bitcoinservice.service;

import org.sep.bitcoinservice.exceptions.NoMerchantFoundException;
import org.sep.bitcoinservice.model.Merchant;
import org.sep.bitcoinservice.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Stream;

@Service
public class MerchantServiceImpl implements MerchantService {

    private MerchantRepository merchantRepository;

    @Autowired
    public MerchantServiceImpl(MerchantRepository merchantRepository){
        this.merchantRepository = merchantRepository;
    }

    @Override
    public Merchant findByIssn(String issn) {
        Merchant merchant = this.merchantRepository.findByIssn(issn);
        if (merchant != null) {
            return merchant;
        }else{
            throw new NoMerchantFoundException(issn);
        }
    }

    @Override
    public Merchant save(Merchant merchant) {
        if (Stream.of(merchant, merchant.getToken(), merchant.getIssn())
                .anyMatch(Objects::isNull)) {
            return null;
        }
        return this.merchantRepository.save(merchant);
    }
}
