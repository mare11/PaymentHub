package org.sep.sellerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.payment.entity.NotifyPaymentMethodRegistrationDto;
import org.sep.paymentgatewayservice.seller.api.MerchantAlreadyExistsException;
import org.sep.paymentgatewayservice.seller.api.MerchantPaymentMethods;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.sellerservice.dto.MerchantPaymentMethodsDto;
import org.sep.sellerservice.dto.MerchantRegistrationResponse;
import org.sep.sellerservice.dto.PaymentMethodDto;
import org.sep.sellerservice.exceptions.MerchantIsAlreadyEnabledException;
import org.sep.sellerservice.exceptions.NoChosenPaymentMethodException;
import org.sep.sellerservice.exceptions.NoMerchantFoundException;
import org.sep.sellerservice.exceptions.NoPaymentFoundException;
import org.sep.sellerservice.model.*;
import org.sep.sellerservice.repository.MerchantPaymentMethodRepository;
import org.sep.sellerservice.repository.MerchantRepository;
import org.sep.sellerservice.repository.PaymentMethodRepository;
import org.sep.sellerservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantPaymentMethodRepository merchantPaymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentGatewayServiceApi paymentGatewayServiceApi;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public MerchantServiceImpl(final MerchantRepository merchantRepository, final MerchantPaymentMethodRepository merchantPaymentMethodRepository, final PaymentRepository paymentRepository, final PaymentMethodRepository paymentMethodRepository, final PaymentGatewayServiceApi paymentGatewayServiceApi) {
        this.merchantRepository = merchantRepository;
        this.merchantPaymentMethodRepository = merchantPaymentMethodRepository;
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
            log.error("Merchant with id '{}' is not found", merchant.getId());
            throw new NoMerchantFoundException(merchant.getId());
        }

        this.merchantRepository.save(merchant);

        log.info("Merchant with id '{}' is updated successfully", merchant.getId());
    }

    @Override
    public MerchantRegistrationResponse addPaymentMethods(final MerchantPaymentMethodsDto merchantPaymentMethodsDto) throws NoMerchantFoundException {
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

        if (merchantPaymentMethodsDto.getPaymentMethods().isEmpty()) {
            return MerchantRegistrationResponse.builder()
                    .successFlag(false)
                    .message("Please, choose at least one payment method.")
                    .build();
        }

        final MerchantPaymentMethods merchantPaymentMethods = MerchantPaymentMethods.builder()
                .merchantId(merchant.getId())
                .returnUrl(merchant.getReturnUrl())
                .paymentMethods(merchantPaymentMethodsDto.getPaymentMethods())
                .build();

        final Map<Long, String> registrationUrlsMap;
        try {
            log.info("Retrieving registration URLs from payment services for merchant with id '{}'", merchantPaymentMethods.getMerchantId());
            registrationUrlsMap = this.paymentGatewayServiceApi.retrievePaymentMethodsRegistrationUrls(merchantPaymentMethods).getBody();

            if (registrationUrlsMap == null || registrationUrlsMap.isEmpty() || registrationUrlsMap.values().stream().anyMatch(Objects::isNull)) {
                log.error("Retrieving registration URLs from payment services for merchant with id '{}' has failed", merchantPaymentMethods.getMerchantId());
                return MerchantRegistrationResponse.builder()
                        .successFlag(false)
                        .message("Retrieving URLs from payment services has failed! Please, try again.")
                        .build();
            }

        } catch (final Exception e) {
            log.error("Retrieving registration URLs from payment services for merchant with id '{}' has failed", merchantPaymentMethods.getMerchantId());
            return MerchantRegistrationResponse.builder()
                    .successFlag(false)
                    .message("Retrieving URLs from payment services has failed! Please, try again.")
                    .build();
        }

        log.info("Registration URLs are retrieved successfully from payment services for merchant with id '{}'", merchantPaymentMethods.getMerchantId());

        merchantPaymentMethods.getPaymentMethods().forEach(method -> {
            final MerchantPaymentMethodId merchantPaymentMethodId = MerchantPaymentMethodId.builder()
                    .merchant(merchant)
                    .paymentMethodEntity(this.paymentMethodRepository.getOne(method.getId()))
                    .build();
            final MerchantPaymentMethod merchantPaymentMethod = MerchantPaymentMethod.builder()
                    .id(merchantPaymentMethodId)
                    .credentialsProvided(false)
                    .registrationUrl(registrationUrlsMap.get(method.getId()))
                    .build();

            this.merchantPaymentMethodRepository.save(merchantPaymentMethod);

            merchant.getMerchantPaymentMethods().add(merchantPaymentMethod);
        });

        return MerchantRegistrationResponse.builder()
                .successFlag(true)
                .build();
    }

    @Override
    public List<PaymentMethodDto> getMerchantPaymentMethodsRegistrationUrls(final String id) {
        Assert.notNull(id, "Merchant id can't be null.");

        final Merchant merchant = this.findById(id);

        if (merchant == null) {
            log.error("Merchant with id '{}' is not found", id);
            throw new NoMerchantFoundException(id);
        }

        return merchant.getMerchantPaymentMethods().stream().map(merchantPaymentMethod -> PaymentMethodDto.builder()
                .name(merchantPaymentMethod.getId().getPaymentMethodEntity().getName())
                .registrationUrl(merchantPaymentMethod.getRegistrationUrl())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<PaymentMethod> getMerchantPaymentMethods(final String id) {
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
        return merchant.getMerchantPaymentMethods().stream()
                .map(method -> this.modelMapper.map(method.getId().getPaymentMethodEntity(), PaymentMethod.class))
                .collect(Collectors.toList());
    }

    @Override
    public MerchantRegistrationResponse confirmPaymentMethodsRegistration(final String id) {
        Assert.notNull(id, "Merchant id can't be null.");

        final Merchant merchant = this.findById(id);

        if (merchant == null) {
            log.error("Merchant with id '{}' is not found", id);
            throw new NoMerchantFoundException(id);
        }

        if (merchant.getEnabled()) {
            log.error("Merchant with id '{}' is already registered", id);
            throw new MerchantIsAlreadyEnabledException(id);
        }

        final boolean allCredentialsProvided = merchant.getMerchantPaymentMethods().stream().allMatch(MerchantPaymentMethod::getCredentialsProvided);

        final MerchantRegistrationResponse merchantRegistrationResponse = MerchantRegistrationResponse.builder()
                .successFlag(allCredentialsProvided)
                .message(allCredentialsProvided ? null : "Error! You have not entered credentials for each of chosen payment methods.")
                .returnUrl(allCredentialsProvided ? merchant.getReturnUrl() : null)
                .build();

        if (allCredentialsProvided) {
            this.enableMerchant(id);
        }

        return merchantRegistrationResponse;
    }

    @Override
    public Boolean notifyMerchantIsRegistered(final NotifyPaymentMethodRegistrationDto notifyPaymentMethodRegistrationDto) {
        Assert.notNull(notifyPaymentMethodRegistrationDto, "Notify payment method registration object can't be null!");
        Assert.notNull(notifyPaymentMethodRegistrationDto.getMethodName(), "Payment method name can't be null!");
        Assert.notNull(notifyPaymentMethodRegistrationDto.getMerchantId(), "Merchant id can't be null!");

        log.info("Update status of registration of merchant with id '{}' for payment method named '{}'",
                notifyPaymentMethodRegistrationDto.getMerchantId(),
                notifyPaymentMethodRegistrationDto.getMethodName());

        final PaymentMethodEntity method = this.paymentMethodRepository.findByName(notifyPaymentMethodRegistrationDto.getMethodName());

        if (method == null) {
            log.error("Payment method with name '{}' does not exist", notifyPaymentMethodRegistrationDto.getMethodName());
            return false;
        }

        final MerchantPaymentMethod merchantPaymentMethod = this.merchantPaymentMethodRepository.findById_Merchant_IdAndId_PaymentMethodEntity_Id(notifyPaymentMethodRegistrationDto.getMerchantId(), method.getId());

        if (merchantPaymentMethod == null) {
            log.error("Merchant with id '{}' has not chosen method named '{}",
                    notifyPaymentMethodRegistrationDto.getMerchantId(),
                    notifyPaymentMethodRegistrationDto.getMethodName());
            return false;
        }

        merchantPaymentMethod.setCredentialsProvided(true);
        this.merchantPaymentMethodRepository.save(merchantPaymentMethod);

        log.info("Status of registration of merchant with id '{}' for payment method named '{}' is updated successfully",
                notifyPaymentMethodRegistrationDto.getMerchantId(),
                notifyPaymentMethodRegistrationDto.getMethodName());

        return true;
    }

    private void enableMerchant(final String merchantId) {
        Assert.notNull(merchantId, "Merchant merchant id can't be null.");

        log.info("Enabling merchant with id '{}'", merchantId);

        final Merchant merchant = this.findById(merchantId);

        if (merchant == null) {
            log.error("Merchant with id '{}' is not found", merchantId);
            throw new NoMerchantFoundException(merchantId);
        }

        merchant.setEnabled(true);
        this.update(merchant);

        log.info("Merchant with id '{}' is enabled successfully", merchantId);
    }
}