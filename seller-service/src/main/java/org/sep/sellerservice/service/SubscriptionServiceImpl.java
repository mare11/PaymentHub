package org.sep.sellerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.api.SellerRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionResponse;
import org.sep.sellerservice.dto.CustomerSubscriptionDto;
import org.sep.sellerservice.exceptions.NoSellerFoundException;
import org.sep.sellerservice.exceptions.NoSubscriptionFoundException;
import org.sep.sellerservice.exceptions.NoSubscriptionPlansFoundException;
import org.sep.sellerservice.exceptions.SubscriptionCreationException;
import org.sep.sellerservice.model.Seller;
import org.sep.sellerservice.model.Subscription;
import org.sep.sellerservice.repository.SellerRepository;
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
    private final SellerRepository sellerRepository;
    private final PaymentGatewayServiceApi paymentGatewayServiceApi;

    @Autowired
    public SubscriptionServiceImpl(final SubscriptionRepository subscriptionRepository, final SellerRepository sellerRepository, final PaymentGatewayServiceApi paymentGatewayServiceApi) {
        this.subscriptionRepository = subscriptionRepository;
        this.sellerRepository = sellerRepository;
        this.paymentGatewayServiceApi = paymentGatewayServiceApi;
    }

    @Override
    public Subscription findById(final Long id) {
        return this.subscriptionRepository.findById(id).orElse(null);
    }

    @Override
    public Long prepareSubscription(final SellerRequest sellerRequest) {
        Assert.notNull(sellerRequest, "Seller request object can't be null!");
        Assert.noNullElements(Stream.of(sellerRequest.getIssn(), sellerRequest.getReturnUrl()).toArray(),
                "Both issn and return url must be provided!");

        final Seller seller = this.sellerRepository.findByIssn(sellerRequest.getIssn());
        log.info("Seller with issn: {} is retrieved", sellerRequest.getIssn());

        if (seller == null || !seller.getEnabled()) {
            log.error("Seller is not found or is not enabled");
            throw new NoSellerFoundException(sellerRequest.getIssn());
        }

        Subscription subscription = Subscription.builder()
                .returnUrl(sellerRequest.getReturnUrl())
                .seller(seller)
                .build();
        subscription = this.subscriptionRepository.save(subscription);

        log.info("Subscription with id '{}' is saved into DB successfully", subscription.getId());

        return subscription.getId();
    }

    @Override
    public List<SubscriptionPlan> retrieveSubscriptionPlans(final Long id) {
        Assert.notNull(id, "Subscription id can't be null!");

        log.info("Retrieving subscription plans for subscription with id '{}'", id);

        final Subscription subscription = this.findById(id);

        if (subscription == null) {
            throw new NoSubscriptionFoundException(id);
        }

        log.info("Subscription with id '{}' is retrieved from DB", id);

        final List<SubscriptionPlan> subscriptionPlans = this.paymentGatewayServiceApi.retrieveSubscriptionPlans(subscription.getSeller().getIssn()).getBody();

        if (subscriptionPlans == null) {
            throw new NoSubscriptionPlansFoundException(subscription.getSeller().getIssn());
        }

        log.info("{} subscription plans are retrieved successfully for merchant with id '{}'", subscriptionPlans.size(), subscription.getSeller().getIssn());

        return subscriptionPlans;
    }

    @Override
    public SubscriptionResponse createSubscription(final CustomerSubscriptionDto customerSubscriptionDto) {
        Assert.notNull(customerSubscriptionDto, "Subscription object can't be null!");
        Assert.noNullElements(Stream.of(customerSubscriptionDto.getId(), customerSubscriptionDto.getPlan()).toArray(),
                "Both id and subscription plan must be provided!");

        Subscription subscription = this.findById(customerSubscriptionDto.getId());

        if (subscription == null) {
            throw new NoSubscriptionFoundException(customerSubscriptionDto.getId());
        }

        subscription.setPlanId(customerSubscriptionDto.getPlan().getPlanId());
        subscription.setIntervalUnit(customerSubscriptionDto.getPlan().getIntervalUnit());
        subscription.setPrice(customerSubscriptionDto.getPlan().getPrice());
        subscription.setSetupFee(customerSubscriptionDto.getPlan().getSetupFee());
        subscription = this.subscriptionRepository.save(subscription);

        log.info("Subscription with id '{}' is updated successfully", subscription.getId());

        final SubscriptionRequest subscriptionRequest = SubscriptionRequest.builder()
                .merchantId(subscription.getSeller().getIssn())
                .merchantName(subscription.getSeller().getName())
                .planId(customerSubscriptionDto.getPlan().getPlanId())
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