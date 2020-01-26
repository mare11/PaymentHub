package org.sep.sellerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.seller.api.MerchantAlreadyExistsException;
import org.sep.paymentgatewayservice.seller.api.MerchantPaymentMethods;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.sellerservice.dto.MerchantPaymentMethodsDto;
import org.sep.sellerservice.exceptions.NoChosenPaymentMethodException;
import org.sep.sellerservice.exceptions.NoMerchantFoundException;
import org.sep.sellerservice.exceptions.NoPaymentFoundException;
import org.sep.sellerservice.model.Merchant;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.repository.MerchantRepository;
import org.sep.sellerservice.repository.PaymentMethodRepository;
import org.sep.sellerservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentGatewayServiceApi paymentGatewayServiceApi;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public MerchantServiceImpl(final MerchantRepository merchantRepository, final PaymentRepository paymentRepository, final PaymentMethodRepository paymentMethodRepository, final PaymentGatewayServiceApi paymentGatewayServiceApi) {
        this.merchantRepository = merchantRepository;
        this.paymentRepository = paymentRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentGatewayServiceApi = paymentGatewayServiceApi;
    }

    @Override
    public Merchant findById(final String id) {
        return this.merchantRepository.findById(id).orElse(null);
    }

    @Override
    public Merchant save(final MerchantRequest merchantRequest) throws MerchantAlreadyExistsException {
        Assert.notNull(merchantRequest, "Merchant registration object can't be null!");
        Assert.noNullElements(
                Stream.of(merchantRequest.getName(),
                        merchantRequest.getReturnUrl())
                        .toArray(),
                "One or more fields are not specified.");

//        if (this.merchantRepository.findByMerchantId(merchantRequest.getMerchantId()) != null)
//            throw new MerchantAlreadyExistsException(merchantRequest.getMerchantId());

        final Merchant merchant = Merchant.builder()
                .name(merchantRequest.getName())
                .returnUrl(merchantRequest.getReturnUrl())
                .enabled(false)
                .build();

        return this.merchantRepository.save(merchant);
    }

    @Override
    public void update(final Merchant merchant) throws NoMerchantFoundException {
        Assert.notNull(merchant, "Merchant object can't be null!");
        Assert.notNull(merchant.getId(), "Merchant id can't be null!");

        if (this.findById(merchant.getId()) == null) {
            throw new NoMerchantFoundException(merchant.getId());
        }

        this.merchantRepository.save(merchant);
    }

    @Override
    public String addPaymentMethods(final MerchantPaymentMethodsDto merchantPaymentMethodsDto) throws NoMerchantFoundException {
        Assert.notNull(merchantPaymentMethodsDto, "Merchant payment methods object can't be null!");
        Assert.noNullElements(
                Stream.of(merchantPaymentMethodsDto.getMerchantId(),
                        merchantPaymentMethodsDto.getPaymentMethods())
                        .toArray(),
                "One or more fields are not specified.");

        if (merchantPaymentMethodsDto.getPaymentMethods().isEmpty()) {
            throw new NoChosenPaymentMethodException();
        }

        final Merchant merchant = this.merchantRepository.findById(merchantPaymentMethodsDto.getMerchantId()).orElse(null);

        if (merchant == null) {
            throw new NoMerchantFoundException(merchantPaymentMethodsDto.getMerchantId());
        }

        final MerchantPaymentMethods merchantPaymentMethods = MerchantPaymentMethods.builder()
                .merchantId(merchant.getId())
                .returnUrl(merchant.getReturnUrl())
                .paymentMethods(merchantPaymentMethodsDto.getPaymentMethods())
                .build();

        merchantPaymentMethods.getPaymentMethods().forEach(method ->
                merchant.getPaymentMethodEntities().add(this.paymentMethodRepository.getOne(method.getId())));

        this.update(merchant);

        return this.paymentGatewayServiceApi.registerPaymentMethods(merchantPaymentMethods).getBody();
    }

    @Override
    public List<PaymentMethod> getSellerPaymentMethods(final String id) {
        Assert.notNull(id, "Payment id can't be null.");

        final Optional<Payment> payment = this.paymentRepository.findById(id);

        if (payment.isEmpty()) {
            log.error("Payment with id '{}' does not exist", id);
            throw new NoPaymentFoundException(id);
        }

        final Merchant merchant = this.findById(payment.get().getMerchant().getId());
        if (merchant == null) {
            return null;
        }
        return merchant.getPaymentMethodEntities().stream()
                .map(method -> this.modelMapper.map(method, PaymentMethod.class))
                .collect(Collectors.toList());
    }

    @Override
    public void enableMerchant(final String merchantId) {
        Assert.notNull(merchantId, "Merchant merchant id can't be null.");

        final Merchant merchant = this.findById(merchantId);

        if (merchant == null) {
            throw new NoMerchantFoundException(merchantId);
        }

        merchant.setEnabled(true);

        this.update(merchant);
    }
}