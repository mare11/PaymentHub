package org.sep.paypalservice.service;

import com.paypal.orders.ApplicationContext;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Money;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paypalservice.dto.CompleteDto;
import org.sep.paypalservice.dto.RedirectionDto;
import org.sep.paypalservice.exceptions.NoPlanFoundException;
import org.sep.paypalservice.exceptions.NoSubscriptionFoundException;
import org.sep.paypalservice.exceptions.RequestCouldNotBeExecutedException;
import org.sep.paypalservice.exceptions.ResourceNotFoundException;
import org.sep.paypalservice.model.*;
import org.sep.paypalservice.repository.PlanEntityRepository;
import org.sep.paypalservice.repository.SubscriptionTransactionRepository;
import org.sep.paypalservice.util.PayPalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final String INITIAL_PLAN_STATUS = "ACTIVE";
    private static final String SETUP_FEE_FAILURE_ACTION = "CONTINUE";
    private static final String TENURE_TYPE = "REGULAR";
    private static final String USER_ACTION = "SUBSCRIBE_NOW";
    private final SubscriptionTransactionRepository subscriptionTransactionRepository;
    private final PlanEntityRepository planEntityRepository;
    private final MerchantPaymentDetailsService merchantPaymentDetailsService;
    private final ModelMapper modelMapper;
    private final PayPalUtil payPalUtil;

    @Autowired
    public SubscriptionServiceImpl(final SubscriptionTransactionRepository subscriptionTransactionRepository, final PlanEntityRepository planEntityRepository, final MerchantPaymentDetailsService merchantPaymentDetailsService, final PayPalUtil payPalUtil) {
        this.subscriptionTransactionRepository = subscriptionTransactionRepository;
        this.planEntityRepository = planEntityRepository;
        this.merchantPaymentDetailsService = merchantPaymentDetailsService;
        this.payPalUtil = payPalUtil;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public SubscriptionResponse createSubscription(final SubscriptionRequest subscriptionRequest) {
        Assert.notNull(subscriptionRequest, "Subscription request can't be null!");
        Assert.noNullElements(
                Stream.of(subscriptionRequest.getId(),
                        subscriptionRequest.getTotalCycles(),
                        subscriptionRequest.getMerchantId(),
                        subscriptionRequest.getReturnUrl())
                        .toArray(),
                "One or more fields are not specified.");

        final MerchantPaymentDetails merchantPaymentDetails = this.merchantPaymentDetailsService.findByMerchantId(subscriptionRequest.getMerchantId());

        final Optional<PlanEntity> planEntityOptional = this.planEntityRepository.findById(subscriptionRequest.getId());

        if (planEntityOptional.isEmpty()) {
            throw new NoPlanFoundException(subscriptionRequest.getId());
        }

        final PlanEntity planEntity = planEntityOptional.get();

        final String planId = this.createPlan(planEntity, subscriptionRequest.getTotalCycles(), merchantPaymentDetails);

        final SubscriptionsCreateRequest subscriptionsCreateRequest = this.createSubscriptionRequest(subscriptionRequest, planId);

        final Subscription subscription = this.payPalUtil.sendRequest(subscriptionsCreateRequest, merchantPaymentDetails);

        final LinkDescription approveLink = subscription.getLinks().stream().filter(link -> link.rel().equals(PayPalUtil.APPROVE_REL)).findFirst().orElse(null);
        log.info("Approve link is retrieved...");

        final SubscriptionTransaction subscriptionTransaction = SubscriptionTransaction.builder()
                .planId(planId)
                .subscriptionId(subscription.getId())
                .merchantSubscriptionId(subscriptionRequest.getMerchantSubscriptionId())
                .status(SubscriptionStatus.valueOf(subscription.getStatus()))
                .totalCycles(subscriptionRequest.getTotalCycles())
                .returnUrl(subscriptionRequest.getReturnUrl())
                .planEntity(planEntity)
                .build();

        this.subscriptionTransactionRepository.save(subscriptionTransaction);
        log.info("Subscription transaction is saved...");

        return SubscriptionResponse.builder()
                .subscriptionId(subscriptionTransaction.getSubscriptionId())
                .status(subscription.getStatus().equals(CreateSubscriptionStatus.APPROVAL_PENDING.name()) ? CreateSubscriptionStatus.APPROVAL_PENDING : CreateSubscriptionStatus.SUSPENDED)
                .redirectionUrl(approveLink != null ? approveLink.href() : subscriptionRequest.getReturnUrl())
                .build();
    }

    private String createPlan(final PlanEntity planEntity, final int totalCycles, final MerchantPaymentDetails merchantPaymentDetails) {
        log.info("Creating plan for plan entity with id '{}', interval unit '{}', interval count {} and total cycles {}",
                planEntity.getId(),
                planEntity.getIntervalUnit(),
                planEntity.getIntervalCount(),
                totalCycles);

        final PaymentPreferences paymentPreferences = PaymentPreferences.builder()
                .autoBillOutstanding(true)
                .setupFeeFailureAction(SETUP_FEE_FAILURE_ACTION)
                .paymentFailureThreshold(3)
                .build();

        if (planEntity.getSetupFee() != null && planEntity.getSetupFee() > 0) {
            final Money money = new Money().currencyCode(PayPalUtil.DEFAULT_CURRENCY).value(String.valueOf(planEntity.getSetupFee()));
            paymentPreferences.setSetupFee(money);
        }

        final PricingScheme pricingScheme = PricingScheme.builder()
                .fixedPrice(new Money().currencyCode(PayPalUtil.DEFAULT_CURRENCY)
                        .value(String.valueOf(planEntity.getPrice())))
                .build();

        final Frequency frequency = Frequency.builder()
                .intervalUnit(planEntity.getIntervalUnit())
                .intervalCount(planEntity.getIntervalCount())
                .build();

        final BillingCycle billingCycle = BillingCycle.builder()
                .pricingScheme(pricingScheme)
                .frequency(frequency)
                .tenureType(TENURE_TYPE)
                .sequence(1)
                .totalCycles(totalCycles)
                .build();

        Plan plan = Plan.builder()
                .productId(planEntity.getProductId())
                .name("Subscription plan for product with name ".concat(planEntity.getMerchant().getMerchantId()))
                .status(INITIAL_PLAN_STATUS)
                .billingCycles(Collections.singletonList(billingCycle))
                .paymentPreferences(paymentPreferences)
                .build();

        final PostRequest<Plan> plansCreateRequest = new PlansCreateRequest()
                .prefer(PayPalUtil.PREFER_HEADER)
                .requestBody(plan);

        plan = this.payPalUtil.sendRequest(plansCreateRequest, merchantPaymentDetails);

        log.info("Plan with id '{}' and name '{}' is created successfully", plan.getId(), plan.getName());

        return plan.getId();
    }

    private SubscriptionsCreateRequest createSubscriptionRequest(final SubscriptionRequest subscriptionRequest, final String planId) {

        final ApplicationContext applicationContext = new ApplicationContext()
                .brandName(subscriptionRequest.getMerchantName() == null ? subscriptionRequest.getMerchantId()
                        : subscriptionRequest.getMerchantName().concat(" (").concat(subscriptionRequest.getMerchantId()).concat(")"))
                .userAction(USER_ACTION)
                .locale(PayPalUtil.SERBIAN_LOCALE)
                .returnUrl(PayPalUtil.HTTPS_PREFIX + this.payPalUtil.SERVER_ADDRESS + ":" + this.payPalUtil.FRONTEND_PORT + "/success_subscription")
                .cancelUrl(PayPalUtil.HTTPS_PREFIX + this.payPalUtil.SERVER_ADDRESS + ":" + this.payPalUtil.FRONTEND_PORT + "/cancel_subscription");

        final Subscription subscription = Subscription.builder()
                .planId(planId)
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
    public void updateTransaction(final SubscriptionTransaction subscriptionTransaction) {
        Assert.notNull(subscriptionTransaction, "Subscription transaction can't be null!");

        if (this.subscriptionTransactionRepository.findById(subscriptionTransaction.getId()).isEmpty()) {
            log.error("Subscription transaction with id '{}' does not exist", subscriptionTransaction.getId());
            throw new NoSubscriptionFoundException(subscriptionTransaction.getSubscriptionId());
        }

        this.subscriptionTransactionRepository.save(subscriptionTransaction);
    }

    @Override
    public RedirectionDto completeSubscriptionTransaction(final CompleteDto completeDto) {
        Assert.notNull(completeDto, "Complete object can't be null!");
        Assert.noNullElements(Stream.of(completeDto.getId(), completeDto.getSuccessFlag()).toArray(),
                "Both subscription id and success flag must be provided!");

        log.info("Activating subscription transaction with id '{}'", completeDto.getId());

        final SubscriptionTransaction subscriptionTransaction = this.findBySubscriptionId(completeDto.getId());

        if (subscriptionTransaction == null) {
            log.error("Subscription transaction with id '{}' does not exist", completeDto.getId());
            throw new NoSubscriptionFoundException(completeDto.getId());
        }

        if (completeDto.getSuccessFlag() && !subscriptionTransaction.getStatus().equals(SubscriptionStatus.ACTIVE)) {
            final SubscriptionsGetRequest subscriptionsGetRequest = new SubscriptionsGetRequest(subscriptionTransaction.getSubscriptionId());
            final Subscription subscription = this.payPalUtil.sendRequest(subscriptionsGetRequest, this.merchantPaymentDetailsService.findByMerchantId(subscriptionTransaction.getPlanEntity().getMerchant().getMerchantId()));

            subscriptionTransaction.setSubscriberName(subscription.getSubscriber() != null && subscription.getSubscriber().getName() != null
                    ? subscription.getSubscriber().getName().givenName().concat(" ").concat(subscription.getSubscriber().getName().surname())
                    : null);
            this.updateTransactionStatus(subscriptionTransaction, SubscriptionStatus.ACTIVE);

            log.info("Subscription transaction with id '{}' is activated successfully", completeDto.getId());
        }

        return RedirectionDto.builder().redirectionUrl(subscriptionTransaction.getReturnUrl()).build();
    }

    @Override
    public SubscriptionCancelResponse cancelSubscription(final SubscriptionCancelRequest subscriptionCancelRequest) {
        if (subscriptionCancelRequest == null) {
            log.error("Canceling is not executed because provided subscription cancel request object is null");
            return SubscriptionCancelResponse.builder()
                    .cancellationFlag(false)
                    .cancellationMessage("Error! Invalid canceling data!")
                    .build();
        }

        log.info("Canceling subscription with merchant subscription id '{}'. Reason of canceling: {}",
                subscriptionCancelRequest.getMerchantSubscriptionId(),
                subscriptionCancelRequest.getCancelingReason());

        if (subscriptionCancelRequest.getMerchantSubscriptionId() == null ||
                subscriptionCancelRequest.getMerchantSubscriptionId().isEmpty() ||
                subscriptionCancelRequest.getCancelingReason() == null ||
                subscriptionCancelRequest.getCancelingReason().isEmpty()) {
            log.error("Canceling is not executed because provided merchant subscription id and/or canceling reason are null");
            return SubscriptionCancelResponse.builder()
                    .cancellationFlag(false)
                    .cancellationMessage("Error! Please choose existing subscription and enter canceling reason.")
                    .build();
        }

        final SubscriptionTransaction subscriptionTransaction
                = this.subscriptionTransactionRepository.findByMerchantSubscriptionId(subscriptionCancelRequest.getMerchantSubscriptionId());

        if (subscriptionTransaction == null) {
            log.error("Canceling is not executed because subscription with merchant subscription id '{}' does not exist", subscriptionCancelRequest.getMerchantSubscriptionId());
            return SubscriptionCancelResponse.builder()
                    .cancellationFlag(false)
                    .cancellationMessage("Error! Chosen subscription does not exist.")
                    .build();
        }

        if (subscriptionTransaction.getStatus().equals(SubscriptionStatus.CANCELLED) ||
                subscriptionTransaction.getStatus().equals(SubscriptionStatus.EXPIRED) ||
                subscriptionTransaction.getStatus().equals(SubscriptionStatus.SUSPENDED)) {
            log.error("Canceling is not executed because subscription with merchant subscription id '{}' is already canceled, expired or suspended", subscriptionCancelRequest.getMerchantSubscriptionId());
            return SubscriptionCancelResponse.builder()
                    .cancellationFlag(false)
                    .cancellationMessage("Error! Chosen subscription is already canceled, expired or suspended.")
                    .build();
        }

        final PostRequest<Subscription> subscriptionsCancelRequest = new SubscriptionsCancelRequest(subscriptionTransaction.getSubscriptionId())
                .requestBody(SubscriptionCancel.builder().reason(subscriptionCancelRequest.getCancelingReason()).build());

        try {
            this.payPalUtil.sendRequest(subscriptionsCancelRequest, this.merchantPaymentDetailsService.findByMerchantId(subscriptionTransaction.getPlanEntity().getMerchant().getMerchantId()));
        } catch (final RequestCouldNotBeExecutedException | ResourceNotFoundException e) {
            log.error("Canceling of subscription with merchant subscription id '{}' is not executed because request to PayPal API has failed", subscriptionCancelRequest.getMerchantSubscriptionId());
            return SubscriptionCancelResponse.builder()
                    .cancellationFlag(false)
                    .cancellationMessage("Error! Canceling failed due to problem in communication with PayPal API. Please try again later.")
                    .build();
        }

        subscriptionTransaction.setStatus(SubscriptionStatus.CANCELLED);
        this.updateTransaction(subscriptionTransaction);

        log.info("Subscription with merchant subscription id '{}' is canceled successfully", subscriptionCancelRequest.getMerchantSubscriptionId());

        return SubscriptionCancelResponse.builder()
                .cancellationFlag(true)
                .build();
    }

    @Override
    @Scheduled(initialDelay = PayPalUtil.SCHEDULER_INITIAL_DELAY_IN_SECONDS * 1000, fixedDelay = PayPalUtil.SCHEDULER_DELAY_IN_SECONDS * 1000)
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
            try {
                final Subscription subscription = this.payPalUtil.sendRequest(subscriptionsGetRequest, this.merchantPaymentDetailsService.findByMerchantId(subscriptionTransaction.getPlanEntity().getMerchant().getMerchantId()));
                if (!subscription.getStatus().equals(subscriptionStatus.name())) {
                    this.updateTransactionStatus(subscriptionTransaction, SubscriptionStatus.valueOf(subscription.getStatus()));
                }
            } catch (final ResourceNotFoundException e) {
                log.info("Subscription with id {} could not be found", subscriptionTransaction.getSubscriptionId());
                this.updateTransactionStatus(subscriptionTransaction, SubscriptionStatus.SUSPENDED);
            } catch (final RequestCouldNotBeExecutedException e) {
                log.info("Request to PayPal API for subscription with id {} has failed because of communication problems", subscriptionTransaction.getSubscriptionId());
            }
        });
        log.info("Checking subscription transactions with status {} is completed...", subscriptionStatus.name());
    }

    private void updateTransactionStatus(final SubscriptionTransaction subscriptionTransaction, final SubscriptionStatus status) {
        final SubscriptionStatus subscriptionStatusToBeChanged = subscriptionTransaction.getStatus();
        log.info("Update status of subscription transaction with subscription id '{}' from {} to {}",
                subscriptionTransaction.getSubscriptionId(),
                subscriptionStatusToBeChanged.name(),
                status.name());
        subscriptionTransaction.setStatus(status);
        this.updateTransaction(subscriptionTransaction);
        log.info("Status of subscription transaction with subscription id '{}' is successfully updated from {} to {}",
                subscriptionTransaction.getSubscriptionId(),
                subscriptionStatusToBeChanged.name(),
                status.name());
    }
}