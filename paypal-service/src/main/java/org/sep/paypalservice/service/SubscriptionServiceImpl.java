package org.sep.paypalservice.service;

import com.paypal.orders.ApplicationContext;
import com.paypal.orders.LinkDescription;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sep.paymentgatewayservice.payment.entity.CreateSubscriptionStatus;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionResponse;
import org.sep.paypalservice.exceptions.NoSubscriptionFoundException;
import org.sep.paypalservice.model.*;
import org.sep.paypalservice.repository.SubscriptionTransactionRepository;
import org.sep.paypalservice.util.PayPalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    @Value("${ip.address}")
    private String SERVER_ADDRESS;
    @Value("${frontend-port}")
    private String FRONTEND_PORT;
    private static final String USER_ACTION = "SUBSCRIBE_NOW";
    private static final int SCHEDULER_DELAY_IN_SECONDS = 30;
    private final SubscriptionTransactionRepository subscriptionTransactionRepository;
    private final MerchantPaymentDetailsService merchantPaymentDetailsService;
    private final ModelMapper modelMapper;
    private final PayPalUtil payPalUtil;

    @Autowired
    public SubscriptionServiceImpl(final SubscriptionTransactionRepository subscriptionTransactionRepository, final MerchantPaymentDetailsService merchantPaymentDetailsService, final PayPalUtil payPalUtil) {
        this.subscriptionTransactionRepository = subscriptionTransactionRepository;
        this.merchantPaymentDetailsService = merchantPaymentDetailsService;
        this.payPalUtil = payPalUtil;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public SubscriptionResponse createSubscription(final SubscriptionRequest subscriptionRequest) {
        Assert.notNull(subscriptionRequest, "Subscription request can't be null!");
        Assert.noNullElements(
                Stream.of(subscriptionRequest.getPlanId(),
                        subscriptionRequest.getMerchantId(),
                        subscriptionRequest.getReturnUrl())
                        .toArray(),
                "One or more fields are not specified.");

        final MerchantPaymentDetails merchantPaymentDetails = this.merchantPaymentDetailsService.findByMerchantId(subscriptionRequest.getMerchantId());

        final SubscriptionsCreateRequest subscriptionsCreateRequest = this.createSubscriptionRequest(subscriptionRequest);

        final Subscription subscription = this.payPalUtil.sendRequest(subscriptionsCreateRequest, merchantPaymentDetails);

        final LinkDescription approveLink = subscription.getLinks().stream().filter(link -> link.rel().equals(PayPalUtil.APPROVE_REL)).findFirst().orElse(null);
        log.info("Approve link is retrieved...");

        final SubscriptionTransaction subscriptionTransaction = SubscriptionTransaction.builder()
                .subscriptionId(subscription.getId())
                .status(SubscriptionStatus.valueOf(subscription.getStatus()))
                .merchantId(merchantPaymentDetails.getMerchantId())
                .returnUrl(subscriptionRequest.getReturnUrl())
                .build();

        this.subscriptionTransactionRepository.save(subscriptionTransaction);
        log.info("Subscription transaction is saved...");

        return SubscriptionResponse.builder()
                .subscriptionId(subscriptionTransaction.getSubscriptionId())
                .status(subscription.getStatus().equals(CreateSubscriptionStatus.APPROVAL_PENDING.name()) ? CreateSubscriptionStatus.APPROVAL_PENDING : CreateSubscriptionStatus.SUSPENDED)
                .redirectionUrl(approveLink != null ? approveLink.href() : subscriptionRequest.getReturnUrl())
                .build();
    }

    private SubscriptionsCreateRequest createSubscriptionRequest(final SubscriptionRequest subscriptionRequest) {

        final ApplicationContext applicationContext = new ApplicationContext()
                .brandName(subscriptionRequest.getMerchantName() == null ? subscriptionRequest.getMerchantId()
                        : subscriptionRequest.getMerchantName().concat(" (").concat(subscriptionRequest.getMerchantId()).concat(")"))
                .userAction(USER_ACTION)
                .locale(PayPalUtil.SERBIAN_LOCALE)
                .returnUrl(PayPalUtil.HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/success_subscription")
                .cancelUrl(PayPalUtil.HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/cancel_subscription");

        final Subscription subscription = Subscription.builder()
                .planId(subscriptionRequest.getPlanId())
                .applicationContext(applicationContext)
                .build();

        return (SubscriptionsCreateRequest) new SubscriptionsCreateRequest().prefer(PayPalUtil.PREFER_HEADER).requestBody(subscription);
    }

    @Override
    public List<SubscriptionPlan> retrieveSubscriptionPlans(final String merchantId) {
        Assert.notNull(merchantId, "Merchant id can't be null!");

        final MerchantPaymentDetails merchantPaymentDetails = this.merchantPaymentDetailsService.findByMerchantId(merchantId);

        log.info("Retrieved merchantPaymentDetails object for merchant id '{}'", merchantId);

        final Set<PlanEntity> plans = merchantPaymentDetails.getPlans();
        log.info("Retrieved plans for merchant with id '{}'", merchantId);

        return plans.stream().map(planEntity -> this.modelMapper.map(planEntity, SubscriptionPlan.class)).collect(Collectors.toList());
    }

    @Override
    public SubscriptionTransaction findBySubscriptionId(final String subscriptionId) {
        return this.subscriptionTransactionRepository.findBySubscriptionId(subscriptionId);
    }

    @Override
    public SubscriptionTransaction updateTransaction(final SubscriptionTransaction subscriptionTransaction) {
        Assert.notNull(subscriptionTransaction, "Subscription transaction can't be null!");

        if (this.subscriptionTransactionRepository.findById(subscriptionTransaction.getId()).isEmpty()) {
            log.error("Subscription transaction with id '{}' does not exist", subscriptionTransaction.getId());
            throw new NoSubscriptionFoundException(subscriptionTransaction.getSubscriptionId());
        }

        return this.subscriptionTransactionRepository.save(subscriptionTransaction);
    }

    @Override
    @Scheduled(fixedDelay = SCHEDULER_DELAY_IN_SECONDS * 1000)
    public void checkUnfinishedTransactions() {
        this.checkTransactionsByStatus(SubscriptionStatus.APPROVAL_PENDING);
        this.checkTransactionsByStatus(SubscriptionStatus.APPROVED);
        this.checkTransactionsByStatus(SubscriptionStatus.ACTIVE);
    }

    private void checkTransactionsByStatus(final SubscriptionStatus subscriptionStatus) {
        log.info("Checking subscription transactions with status {}...", subscriptionStatus.name());
        final List<SubscriptionTransaction> subscriptionTransactions = this.subscriptionTransactionRepository.findAllByStatus(subscriptionStatus);
        subscriptionTransactions.forEach(subscriptionTransaction -> {
            log.info("Checking subscription transaction with subscription id {}...", subscriptionTransaction.getSubscriptionId());
            final SubscriptionsGetRequest subscriptionsGetRequest = new SubscriptionsGetRequest(subscriptionTransaction.getSubscriptionId());
            final Subscription subscription = this.payPalUtil.sendRequest(subscriptionsGetRequest, this.merchantPaymentDetailsService.findByMerchantId(subscriptionTransaction.getMerchantId()));
            if (!subscription.getStatus().equals(subscriptionStatus.name())) {
                log.info("Update status of subscription transaction with subscription id '{}' from {} to {}",
                        subscriptionTransaction.getSubscriptionId(),
                        subscriptionStatus.name(),
                        subscription.getStatus());
                subscriptionTransaction.setStatus(SubscriptionStatus.valueOf(subscription.getStatus()));
                this.updateTransaction(subscriptionTransaction);
                log.info("Status of subscription transaction with subscription id '{}' is successfully updated from {} to {}",
                        subscriptionTransaction.getSubscriptionId(),
                        subscriptionStatus.name(),
                        subscription.getStatus());
            }
        });
        log.info("Checking subscription transactions with status {} is completed...", subscriptionStatus.name());
    }
}