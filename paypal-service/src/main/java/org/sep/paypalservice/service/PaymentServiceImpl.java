package org.sep.paypalservice.service;

import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.payment.entity.CreatePaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.dto.RegistrationDto;
import org.sep.paypalservice.exceptions.NoMerchantFoundException;
import org.sep.paypalservice.exceptions.NoOrderFoundException;
import org.sep.paypalservice.exceptions.RequestCouldNotBeExecutedException;
import org.sep.paypalservice.model.*;
import org.sep.paypalservice.repository.PaymentTransactionRepository;
import org.sep.paypalservice.repository.PlanEntityRepository;
import org.sep.paypalservice.util.PayPalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.stream.Stream;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Value("${ip.address}")
    private String SERVER_ADDRESS;
    @Value("${frontend-port}")
    private String FRONTEND_PORT;
    private static final String INTENT = "CAPTURE";
    private static final String DEFAULT_CURRENCY = "USD";
    private static final String ITEM_CATEGORY = "DIGITAL_GOODS";
    private static final String DIGITAL = "DIGITAL";
    private static final String MAGAZINES = "MAGAZINES";
    private static final String INITIAL_PLAN_STATUS = "ACTIVE";
    private static final String SETUP_FEE_FAILURE_ACTION = "CONTINUE";
    private static final String TENURE_TYPE = "REGULAR";
    private static final int SCHEDULER_DELAY_IN_SECONDS = 30;
    private static final int TIMER_DELAY_IN_MINUTES = 30;
    private final MerchantPaymentDetailsService merchantPaymentDetailsService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentMethodRegistrationApi paymentMethodRegistrationApi;
    private final PlanEntityRepository planEntityRepository;
    private final PayPalUtil payPalUtil;
    private final Timer timer;

    @Autowired
    public PaymentServiceImpl(final MerchantPaymentDetailsService merchantPaymentDetailsService, final PaymentTransactionRepository paymentTransactionRepository, final PaymentMethodRegistrationApi paymentMethodRegistrationApi, final PlanEntityRepository planEntityRepository, final PayPalUtil payPalUtil) {
        this.merchantPaymentDetailsService = merchantPaymentDetailsService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentMethodRegistrationApi = paymentMethodRegistrationApi;
        this.planEntityRepository = planEntityRepository;
        this.payPalUtil = payPalUtil;
        this.timer = new Timer();
    }

    @Override
    public PaymentResponse createPayment(final PaymentRequest paymentRequest) throws NoMerchantFoundException, RequestCouldNotBeExecutedException {
        Assert.notNull(paymentRequest, "Payment request can't be null!");
        Assert.noNullElements(
                Stream.of(paymentRequest.getSellerIssn(),
                        paymentRequest.getPrice(),
                        paymentRequest.getReturnUrl())
                        .toArray(),
                "One or more fields are not specified.");

        final MerchantPaymentDetails merchantPaymentDetails = this.merchantPaymentDetailsService.findByMerchantId(paymentRequest.getSellerIssn());

        final OrdersCreateRequest request = this.createOrderRequest(paymentRequest);
        log.info("Request is created...");

        final Order order = this.payPalUtil.sendRequest(request, merchantPaymentDetails);

//        this.timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                PaymentServiceImpl.log.info("Timer has occurred for payment transaction with order id '{}'", order.id());
//                final PaymentTransaction paymentTransaction = PaymentServiceImpl.this.findByOrderId(order.id());
//                if (!paymentTransaction.getStatus().equals(TransactionStatus.COMPLETED) && !paymentTransaction.getStatus().equals(TransactionStatus.VOIDED)) {
//                    PaymentServiceImpl.log.info("Canceling payment transaction with order id '{}' because it is not completed yet", order.id());
//                    paymentTransaction.setStatus(TransactionStatus.VOIDED);
//                    PaymentServiceImpl.this.updateTransaction(paymentTransaction);
//                }
//            }
//        }, TIMER_DELAY_IN_MINUTES * 60 * 1000);

        final LinkDescription approveLink = order.links().stream().filter(link -> link.rel().equals(PayPalUtil.APPROVE_REL)).findFirst().orElse(null);
        log.info("Approve link is retrieved...");

        final PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .orderId(order.id())
                .status(TransactionStatus.valueOf(order.status()))
                .merchantId(paymentRequest.getSellerIssn())
                .item(order.purchaseUnits().get(0).items().get(0).name())
                .description(order.purchaseUnits().get(0).items().get(0).description())
                .value(Double.valueOf(order.purchaseUnits().get(0).amountWithBreakdown().value()))
                .currency(order.purchaseUnits().get(0).amountWithBreakdown().currencyCode())
                .returnUrl(paymentRequest.getReturnUrl())
                .build();

        this.paymentTransactionRepository.save(paymentTransaction);
        log.info("Payment transaction is saved...");

        return PaymentResponse.builder()
                .paymentUrl(approveLink != null ? approveLink.href() : paymentRequest.getReturnUrl())
                .orderId(order.id())
                .status(order.status().equals(CreatePaymentStatus.CREATED.name()) ? CreatePaymentStatus.CREATED : CreatePaymentStatus.ERROR)
                .build();
    }

    private OrdersCreateRequest createOrderRequest(final PaymentRequest paymentRequest) {

        final Money money = new Money()
                .currencyCode(paymentRequest.getPriceCurrency() == null ? DEFAULT_CURRENCY : paymentRequest.getPriceCurrency())
                .value(String.format("%.2f", paymentRequest.getPrice()));

        final AmountBreakdown amountBreakdown = new AmountBreakdown().itemTotal(money);

        final AmountWithBreakdown amountWithBreakdown = new AmountWithBreakdown()
                .currencyCode(money.currencyCode())
                .value(money.value())
                .amountBreakdown(amountBreakdown);

        final Item item = new Item()
                .name(paymentRequest.getItem() == null ? "" : paymentRequest.getItem())
                .category(ITEM_CATEGORY)
                .description(paymentRequest.getDescription() == null ? "" : paymentRequest.getDescription())
                .quantity("1")
                .unitAmount(money);

        final List<Item> items = new ArrayList<>();
        items.add(item);

        final PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .amountWithBreakdown(amountWithBreakdown)
                .items(items);

        final List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
        purchaseUnitRequests.add(purchaseUnitRequest);

        final ApplicationContext applicationContext = new ApplicationContext()
                .brandName(paymentRequest.getSellerName() == null ? "" : paymentRequest.getSellerName())
                .locale(PayPalUtil.SERBIAN_LOCALE)
                .returnUrl(PayPalUtil.HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/success_payment")
                .cancelUrl(PayPalUtil.HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/cancel_payment");

        final OrderRequest orderRequest = new OrderRequest()
                .checkoutPaymentIntent(INTENT)
                .purchaseUnits(purchaseUnitRequests)
                .applicationContext(applicationContext);

        return new OrdersCreateRequest()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .prefer(PayPalUtil.PREFER_HEADER)
                .requestBody(orderRequest);
    }

    @Override
    public PaymentTransaction updateTransaction(final PaymentTransaction paymentTransaction) {
        Assert.notNull(paymentTransaction, "Payment transaction can't be null!");

        if (this.paymentTransactionRepository.findById(paymentTransaction.getId()).isEmpty()) {
            log.error("Payment transaction with id '{}' does not exist", paymentTransaction.getId());
            throw new NoOrderFoundException(paymentTransaction.getOrderId());
        }

        return this.paymentTransactionRepository.save(paymentTransaction);
    }

    @Override
    public PaymentTransaction findByOrderId(final String orderId) {
        return this.paymentTransactionRepository.findByOrderId(orderId);
    }

    @Override
    public String retrieveSellerRegistrationUrl(final String merchantId) {
        log.info("Registration page url retrieving...");
        return PayPalUtil.HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/registration/" + merchantId;
    }

    @Override
    public String registerSeller(final RegistrationDto registrationDto) throws RequestCouldNotBeExecutedException {
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
            planEntities = this.createPlans(registrationDto, merchantPaymentDetails);
        }

        this.merchantPaymentDetailsService.save(merchantPaymentDetails);
        planEntities.forEach(planEntity -> {
            planEntity.setMerchant(merchantPaymentDetails);
            this.planEntityRepository.save(planEntity);
            log.info("Plan entity with planId '{}' and interval unit '{}' is saved successfully into DB", planEntity.getPlanId(), planEntity.getIntervalUnit());
        });
        log.info("Merchant payment details are saved into DB...");

        final String returnUrl = this.paymentMethodRegistrationApi.proceedToNextPaymentMethod(merchantPaymentDetails.getMerchantId()).getBody();
        log.info("Proceeding to next payment method is done successfully...");

        return returnUrl;
    }

    @Override
    @Scheduled(fixedDelay = SCHEDULER_DELAY_IN_SECONDS * 1000)
    public void checkUnfinishedTransactions() {
        log.info("Check unfinished payment transactions...");
        final List<PaymentTransaction> createdPaymentTransactions = this.paymentTransactionRepository.findAllByStatus(TransactionStatus.CREATED);
        createdPaymentTransactions.forEach(paymentTransaction -> {
            log.info("Checking payment transaction with order id {}...", paymentTransaction.getOrderId());
            final OrdersGetRequest ordersGetRequest = new OrdersGetRequest(paymentTransaction.getOrderId());
            final Order order = this.payPalUtil.sendRequest(ordersGetRequest, this.merchantPaymentDetailsService.findByMerchantId(paymentTransaction.getMerchantId()));
            if (!order.status().equals(TransactionStatus.CREATED.name())) {
                log.info("Update status of payment transaction with order id '{}' from CREATED to {}", paymentTransaction.getOrderId(), order.status());
                paymentTransaction.setStatus(TransactionStatus.valueOf(order.status()));
                this.updateTransaction(paymentTransaction);
                log.info("Status of payment transaction with order id '{}' is successfully updated from CREATED to {}", paymentTransaction.getOrderId(), order.status());
            }
        });

        final List<PaymentTransaction> approvedPaymentTransactions = this.paymentTransactionRepository.findAllByStatus(TransactionStatus.APPROVED);
        approvedPaymentTransactions.forEach(paymentTransaction -> {
            final OrdersCaptureRequest captureRequest = new OrdersCaptureRequest(paymentTransaction.getOrderId()).prefer(PayPalUtil.PREFER_HEADER);
            log.info("Send capture request for payment transaction with order id '{}'", paymentTransaction.getOrderId());
            final Order order = this.payPalUtil.sendRequest(captureRequest, this.merchantPaymentDetailsService.findByMerchantId(paymentTransaction.getMerchantId()));
            if (!order.status().equals(TransactionStatus.APPROVED.name())) {
                log.info("Checking payment transaction with order id {}...", paymentTransaction.getOrderId());
                log.info("Update status of payment transaction with order id '{}' from APPROVED to {}", paymentTransaction.getOrderId(), order.status());
                paymentTransaction.setStatus(TransactionStatus.valueOf(order.status()));
                this.updateTransaction(paymentTransaction);
                log.info("Status of payment transaction with order id '{}' is successfully updated from APPROVED to {}", paymentTransaction.getOrderId(), order.status());
            }
        });

        log.info("Checking of unfinished payment transactions is completed...");
    }

    private List<PlanEntity> createPlans(final RegistrationDto registrationDto, final MerchantPaymentDetails merchantPaymentDetails) throws RequestCouldNotBeExecutedException {
        log.info("Creation of product with name '{}'", registrationDto.getMerchantId());
        Product product = Product.builder()
                .name(registrationDto.getMerchantId())
                .type(DIGITAL)
                .category(MAGAZINES)
                .build();

        final CreateRequest<Product> productsCreateRequest = new ProductsCreateRequest()
                .prefer(PayPalUtil.PREFER_HEADER)
                .requestBody(product);

        product = this.payPalUtil.sendRequest(productsCreateRequest, merchantPaymentDetails);

        log.info("Product with id '{}' and name '{}' is created successfully", product.getProductId(), product.getName());

        final PaymentPreferences paymentPreferences = PaymentPreferences.builder()
                .autoBillOutstanding(true)
                .setupFeeFailureAction(SETUP_FEE_FAILURE_ACTION)
                .paymentFailureThreshold(3)
                .build();

        if (registrationDto.getSetupFee() != null && registrationDto.getSetupFee() > 0) {
            final Money money = new Money().currencyCode(DEFAULT_CURRENCY).value(String.valueOf(registrationDto.getSetupFee()));
            paymentPreferences.setSetupFee(money);
        }

        final String productId = product.getProductId();

        final List<PlanEntity> planEntities = new ArrayList<>();

        registrationDto.getPlans().forEach(planDto -> {

            final PricingScheme pricingScheme = PricingScheme.builder()
                    .fixedPrice(new Money().currencyCode(DEFAULT_CURRENCY)
                            .value(String.valueOf(planDto.getPrice())))
                    .build();

            final Frequency frequency = Frequency.builder()
                    .intervalUnit(planDto.getPlan())
                    .intervalCount(1)
                    .build();

            final BillingCycle billingCycle = BillingCycle.builder()
                    .pricingScheme(pricingScheme)
                    .frequency(frequency)
                    .tenureType(TENURE_TYPE)
                    .sequence(1)
                    .totalCycles(0)
                    .build();

            Plan plan = Plan.builder()
                    .productId(productId)
                    .name("Subscription plan for product with name ".concat(registrationDto.getMerchantId()))
                    .status(INITIAL_PLAN_STATUS)
                    .billingCycles(Collections.singletonList(billingCycle))
                    .paymentPreferences(paymentPreferences)
                    .build();

            final CreateRequest<Plan> plansCreateRequest = new PlansCreateRequest()
                    .prefer(PayPalUtil.PREFER_HEADER)
                    .requestBody(plan);

            plan = this.payPalUtil.sendRequest(plansCreateRequest, merchantPaymentDetails);

            log.info("Plan with id '{}' and name '{}' is created successfully", plan.getId(), plan.getName());

            final PlanEntity planEntity = PlanEntity.builder()
                    .planId(plan.getId())
                    .intervalUnit(frequency.getIntervalUnit())
                    .price(planDto.getPrice())
                    .setupFee(registrationDto.getSetupFee())
                    .build();

            planEntities.add(planEntity);
        });

        return planEntities;
    }
}