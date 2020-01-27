package org.sep.sellerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionResponse;
import org.sep.sellerservice.dto.CustomerSubscriptionDto;
import org.sep.sellerservice.exceptions.NoMerchantFoundException;
import org.sep.sellerservice.exceptions.NoSubscriptionFoundException;
import org.sep.sellerservice.exceptions.NoSubscriptionPlansFoundException;
import org.sep.sellerservice.exceptions.SubscriptionCreationException;
import org.sep.sellerservice.model.Merchant;
import org.sep.sellerservice.model.Subscription;
import org.sep.sellerservice.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MerchantService merchantService;
    private final PaymentGatewayServiceApi paymentGatewayServiceApi;

    @Autowired
    public SubscriptionServiceImpl(final SubscriptionRepository subscriptionRepository, final MerchantService merchantService, final PaymentGatewayServiceApi paymentGatewayServiceApi) {
        this.subscriptionRepository = subscriptionRepository;
        this.merchantService = merchantService;
        this.paymentGatewayServiceApi = paymentGatewayServiceApi;
    }

    @Override
    public Subscription findById(final String id) {
        return this.subscriptionRepository.findById(id).orElse(null);
    }

    @Override
    public String prepareSubscription(final MerchantRequest merchantRequest) {
        Assert.notNull(merchantRequest, "Merchant request object can't be null!");
        Assert.noNullElements(Stream.of(merchantRequest.getMerchantId(), merchantRequest.getReturnUrl()).toArray(),
                "Both merchant id and return url must be provided!");

        final Merchant merchant = this.merchantService.findById(merchantRequest.getMerchantId());
        log.info("Merchant with merchant id: {} is retrieved", merchantRequest.getMerchantId());

        if (merchant == null || !merchant.getEnabled()) {
            log.error("Merchant is not found or is not enabled");
            throw new NoMerchantFoundException(merchantRequest.getMerchantId());
        }

        final Subscription subscription = Subscription.builder()
                .returnUrl(merchantRequest.getReturnUrl())
                .merchant(merchant)
                .build();

        this.subscriptionRepository.save(subscription);
        log.info("Subscription with id '{}' is saved into DB successfully", subscription.getId());

        return subscription.getId();
    }

    @Override
    public List<SubscriptionPlan> retrieveSubscriptionPlans(final String id) {
        Assert.notNull(id, "Subscription id can't be null!");

        log.info("Retrieving subscription plans for subscription with id '{}'", id);

        final Subscription subscription = this.findById(id);

        if (subscription == null) {
            throw new NoSubscriptionFoundException(id);
        }

        log.info("Subscription with id '{}' is retrieved from DB", id);

        final List<SubscriptionPlan> subscriptionPlans = this.paymentGatewayServiceApi.retrieveSubscriptionPlans(subscription.getMerchant().getId()).getBody();

        if (subscriptionPlans == null) {
            throw new NoSubscriptionPlansFoundException(subscription.getMerchant().getId());
        }

        log.info("{} subscription plans are retrieved successfully for merchant with id '{}'", subscriptionPlans.size(), subscription.getMerchant().getId());

        return subscriptionPlans;
    }

    @Override
    public SubscriptionResponse createSubscription(final CustomerSubscriptionDto customerSubscriptionDto) {
        Assert.notNull(customerSubscriptionDto, "Subscription object can't be null!");
        Assert.noNullElements(Stream.of(customerSubscriptionDto.getMerchantSubscriptionId(), customerSubscriptionDto.getPlan()).toArray(),
                "Both id and subscription plan must be provided!");

        final Subscription subscription = this.findById(customerSubscriptionDto.getMerchantSubscriptionId());

        if (subscription == null) {
            throw new NoSubscriptionFoundException(customerSubscriptionDto.getMerchantSubscriptionId());
        }

        subscription.setPlanId(String.valueOf(customerSubscriptionDto.getPlan().getId()));
        this.subscriptionRepository.save(subscription);

        log.info("Subscription with id '{}' is updated successfully", subscription.getId());

        final SubscriptionRequest subscriptionRequest = SubscriptionRequest.builder()
                .merchantId(subscription.getMerchant().getId())
                .merchantName(subscription.getMerchant().getName())
                .merchantSubscriptionId(subscription.getId())
                .id(customerSubscriptionDto.getPlan().getId())
                .totalCycles(customerSubscriptionDto.getPlan().getTotalCycles())
                .returnUrl(subscription.getReturnUrl())
                .build();

        log.info("Forward subscription on plan with id '{}' to payment gateway", subscription.getPlanId());
        final SubscriptionResponse subscriptionResponse = this.paymentGatewayServiceApi.createSubscription(subscriptionRequest).getBody();

        if (subscriptionResponse == null) {
            throw new SubscriptionCreationException(subscription.getPlanId());
        }

        log.info("Subscription response from payment gateway is retrieved successfully. Subscription id: {}", subscriptionResponse.getSubscriptionId());

        return subscriptionResponse;
    }
}