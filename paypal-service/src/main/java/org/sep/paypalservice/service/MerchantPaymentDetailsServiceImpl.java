package org.sep.paypalservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.sep.paypalservice.dto.RegistrationDto;
import org.sep.paypalservice.exceptions.NoMerchantFoundException;
import org.sep.paypalservice.exceptions.RequestCouldNotBeExecutedException;
import org.sep.paypalservice.model.*;
import org.sep.paypalservice.repository.MerchantPaymentDetailsRepository;
import org.sep.paypalservice.repository.PlanEntityRepository;
import org.sep.paypalservice.util.PayPalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class MerchantPaymentDetailsServiceImpl implements MerchantPaymentDetailsService {

    private static final String DIGITAL = "DIGITAL";
    private static final String MAGAZINES = "MAGAZINES";
    private final MerchantPaymentDetailsRepository merchantPaymentDetailsRepository;
    private final PaymentMethodRegistrationApi paymentMethodRegistrationApi;
    private final PlanEntityRepository planEntityRepository;
    private final PayPalUtil payPalUtil;

    @Autowired
    public MerchantPaymentDetailsServiceImpl(final MerchantPaymentDetailsRepository merchantPaymentDetailsRepository, final PaymentMethodRegistrationApi paymentMethodRegistrationApi, final PlanEntityRepository planEntityRepository, final PayPalUtil payPalUtil) {
        this.merchantPaymentDetailsRepository = merchantPaymentDetailsRepository;
        this.paymentMethodRegistrationApi = paymentMethodRegistrationApi;
        this.planEntityRepository = planEntityRepository;
        this.payPalUtil = payPalUtil;
    }

    @Override
    public MerchantPaymentDetails findByMerchantId(final String merchantId) {
        final MerchantPaymentDetails merchantPaymentDetails = this.merchantPaymentDetailsRepository.findByMerchantId(merchantId);
        if (merchantPaymentDetails == null) {
            log.error("Merchant with id '{}' does not exist", merchantId);
            throw new NoMerchantFoundException(merchantId);
        }
        log.info("Merchant is retrieved from DB...");
        return merchantPaymentDetails;
    }

    @Override
    public void save(MerchantPaymentDetails merchantPaymentDetails) {
        log.info("Saving payment details of merchant with id '{}'...", merchantPaymentDetails.getMerchantId());
        merchantPaymentDetails = this.merchantPaymentDetailsRepository.save(merchantPaymentDetails);
        log.info("Payment details for merchant with id '{} are saved into DB successfully", merchantPaymentDetails.getMerchantId());
    }

    @Override
    public String retrieveMerchantRegistrationUrl(final String merchantId) {
        log.info("Registration page url retrieving...");
        return PayPalUtil.HTTPS_PREFIX + this.payPalUtil.SERVER_ADDRESS + ":" + this.payPalUtil.FRONTEND_PORT + "/registration/" + merchantId;
    }

    @Override
    public String registerMerchant(final RegistrationDto registrationDto) throws RequestCouldNotBeExecutedException {
        Assert.notNull(registrationDto, "Registration dto object can't be null!");
        Assert.noNullElements(
                Stream.of(registrationDto.getClientId(),
                        registrationDto.getClientSecret(),
                        registrationDto.getMerchantId())
                        .toArray(),
                "One or more fields are not specified.");

        final MerchantPaymentDetails merchantPaymentDetails = MerchantPaymentDetails.builder()
                .clientId(registrationDto.getClientId())
                .clientSecret(registrationDto.getClientSecret())
                .merchantId(registrationDto.getMerchantId())
                .build();

        List<PlanEntity> planEntities = new ArrayList<>();
        if (registrationDto.getSubscription()) {
            planEntities = this.createPlanEntities(registrationDto, merchantPaymentDetails);
        }

        this.save(merchantPaymentDetails);
        log.info("Merchant payment details are saved into DB...");
        planEntities.forEach(planEntity -> {
            planEntity.setMerchant(merchantPaymentDetails);
            this.planEntityRepository.save(planEntity);
            log.info("Plan entity with id '{}' and interval unit '{}' and interval count '{}' is saved successfully into DB",
                    planEntity.getId(),
                    planEntity.getIntervalUnit(),
                    planEntity.getIntervalCount());
        });

        final String returnUrl = this.paymentMethodRegistrationApi.proceedToNextPaymentMethod(merchantPaymentDetails.getMerchantId()).getBody();
        log.info("Proceeding to next payment method is done successfully...");

        return returnUrl;
    }

    private List<PlanEntity> createPlanEntities(final RegistrationDto registrationDto, final MerchantPaymentDetails merchantPaymentDetails) throws RequestCouldNotBeExecutedException {
        log.info("Creation of product with name '{}'", registrationDto.getMerchantId());
        Product product = Product.builder()
                .name(registrationDto.getMerchantId())
                .type(DIGITAL)
                .category(MAGAZINES)
                .build();

        final PostRequest<Product> productsCreateRequest = new ProductsCreateRequest()
                .prefer(PayPalUtil.PREFER_HEADER)
                .requestBody(product);

        product = this.payPalUtil.sendRequest(productsCreateRequest, merchantPaymentDetails);

        log.info("Product with id '{}' and name '{}' is created successfully", product.getProductId(), product.getName());

        final List<PlanEntity> planEntities = new ArrayList<>();

        final String productId = product.getProductId();

        registrationDto.getPlans().forEach(planDto -> {
            final PlanEntity planEntity = PlanEntity.builder()
                    .productId(productId)
                    .planName(planDto.getPlan())
                    .intervalUnit(planDto.getIntervalUnit())
                    .intervalCount(planDto.getIntervalCount())
                    .price(planDto.getPrice())
                    .setupFee(registrationDto.getSetupFee())
                    .build();

            planEntities.add(planEntity);
        });
        log.info("Created {} plan entities successfully", planEntities.size());
        return planEntities;
    }
}