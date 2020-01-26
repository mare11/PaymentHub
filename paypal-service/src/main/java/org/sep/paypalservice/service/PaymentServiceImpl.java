package org.sep.paypalservice.service;

import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.payment.entity.CreatePaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.dto.CompleteDto;
import org.sep.paypalservice.dto.RedirectionDto;
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
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Value("${ip.address}")
    private String SERVER_ADDRESS;
    @Value("${frontend-port}")
    private String FRONTEND_PORT;
    private static final String INTENT = "CAPTURE";
    private static final String ITEM_CATEGORY = "DIGITAL_GOODS";
    private static final String DIGITAL = "DIGITAL";
    private static final String MAGAZINES = "MAGAZINES";
    private final MerchantPaymentDetailsService merchantPaymentDetailsService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentMethodRegistrationApi paymentMethodRegistrationApi;
    private final PlanEntityRepository planEntityRepository;
    private final PayPalUtil payPalUtil;

    @Autowired
    public PaymentServiceImpl(final MerchantPaymentDetailsService merchantPaymentDetailsService, final PaymentTransactionRepository paymentTransactionRepository, final PaymentMethodRegistrationApi paymentMethodRegistrationApi, final PlanEntityRepository planEntityRepository, final PayPalUtil payPalUtil) {
        this.merchantPaymentDetailsService = merchantPaymentDetailsService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentMethodRegistrationApi = paymentMethodRegistrationApi;
        this.planEntityRepository = planEntityRepository;
        this.payPalUtil = payPalUtil;
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

        final LinkDescription approveLink = order.links().stream().filter(link -> link.rel().equals(PayPalUtil.APPROVE_REL)).findFirst().orElse(null);
        log.info("Approve link is retrieved...");

        final PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .orderId(order.id())
                .status(TransactionStatus.valueOf(order.status()))
                .merchantId(paymentRequest.getSellerIssn())
                .merchantOrderId("")
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
                .currencyCode(paymentRequest.getPriceCurrency() == null ? PayPalUtil.DEFAULT_CURRENCY : paymentRequest.getPriceCurrency())
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
    public void updateTransaction(final PaymentTransaction paymentTransaction) {
        Assert.notNull(paymentTransaction, "Payment transaction can't be null!");

        if (this.paymentTransactionRepository.findById(paymentTransaction.getId()).isEmpty()) {
            log.error("Payment transaction with id '{}' does not exist", paymentTransaction.getId());
            throw new NoOrderFoundException(paymentTransaction.getOrderId());
        }

        this.paymentTransactionRepository.save(paymentTransaction);
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
            planEntities = this.createPlanEntities(registrationDto, merchantPaymentDetails);
        }

        this.merchantPaymentDetailsService.save(merchantPaymentDetails);
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

    @Override
    public RedirectionDto completePaymentTransaction(final CompleteDto completeDto) {
        Assert.notNull(completeDto, "Complete object can't be null!");
        Assert.noNullElements(Stream.of(completeDto.getId(), completeDto.getSuccessFlag()).toArray(),
                "Both order id and success flag must be provided!");

        log.info("Completing payment transaction with id '{}'", completeDto.getId());

        final PaymentTransaction paymentTransaction = this.findByOrderId(completeDto.getId());

        if (paymentTransaction == null) {
            log.error("Payment transaction with id '{}' does not exist", completeDto.getId());
            throw new NoOrderFoundException(completeDto.getId());
        }

        if (completeDto.getSuccessFlag() && !paymentTransaction.getStatus().equals(TransactionStatus.COMPLETED)) {
            paymentTransaction.setStatus(TransactionStatus.COMPLETED);
            this.paymentTransactionRepository.save(paymentTransaction);

            log.info("Payment transaction with id '{}' is completed successfully", completeDto.getId());
        }

        return RedirectionDto.builder().redirectionUrl(paymentTransaction.getReturnUrl()).build();
    }

    @Override
    @Scheduled(initialDelay = PayPalUtil.SCHEDULER_INITIAL_DELAY_IN_SECONDS * 1000, fixedDelay = PayPalUtil.SCHEDULER_DELAY_IN_SECONDS * 1000)
    public void checkUnfinishedTransactions() {
        log.info("Check unfinished payment transactions...");
        final List<PaymentTransaction> createdPaymentTransactions = this.paymentTransactionRepository.findAllByStatus(TransactionStatus.CREATED);
        createdPaymentTransactions.forEach(paymentTransaction -> {
            log.info("Checking payment transaction with order id {}...", paymentTransaction.getOrderId());
            final OrdersGetRequest ordersGetRequest = new OrdersGetRequest(paymentTransaction.getOrderId());
            try {
                final Order order = this.payPalUtil.sendRequest(ordersGetRequest, this.merchantPaymentDetailsService.findByMerchantId(paymentTransaction.getMerchantId()));
                if (!order.status().equals(TransactionStatus.CREATED.name())) {
                    this.updateTransactionStatus(paymentTransaction, TransactionStatus.valueOf(order.status()));
                }
            } catch (final RequestCouldNotBeExecutedException e) {
                log.info("Order with id {} could not be found", paymentTransaction.getOrderId());
                this.updateTransactionStatus(paymentTransaction, TransactionStatus.VOIDED);
            }
        });

        final List<PaymentTransaction> approvedPaymentTransactions = this.paymentTransactionRepository.findAllByStatus(TransactionStatus.APPROVED);
        approvedPaymentTransactions.forEach(paymentTransaction -> {
            final OrdersCaptureRequest captureRequest = new OrdersCaptureRequest(paymentTransaction.getOrderId()).prefer(PayPalUtil.PREFER_HEADER);
            log.info("Send capture request for payment transaction with order id '{}'", paymentTransaction.getOrderId());
            try {
                final Order order = this.payPalUtil.sendRequest(captureRequest, this.merchantPaymentDetailsService.findByMerchantId(paymentTransaction.getMerchantId()));
                if (!order.status().equals(TransactionStatus.APPROVED.name())) {
                    this.updateTransactionStatus(paymentTransaction, TransactionStatus.valueOf(order.status()));
                }
            } catch (final RequestCouldNotBeExecutedException e) {
                log.info("Order with id {} could not be found", paymentTransaction.getOrderId());
                this.updateTransactionStatus(paymentTransaction, TransactionStatus.VOIDED);
            }
        });

        log.info("Checking of unfinished payment transactions is completed...");
    }

    private void updateTransactionStatus(final PaymentTransaction paymentTransaction, final TransactionStatus status) {
        final TransactionStatus statusToBeChanged = paymentTransaction.getStatus();
        log.info("Update status of payment transaction with order id '{}' from {} to {}", paymentTransaction.getOrderId(), statusToBeChanged.name(), status.name());
        paymentTransaction.setStatus(status);
        this.updateTransaction(paymentTransaction);
        log.info("Status of payment transaction with order id '{}' is successfully updated from {} to {}", paymentTransaction.getOrderId(), statusToBeChanged.name(), status.name());
    }

    private List<PlanEntity> createPlanEntities(final RegistrationDto registrationDto, final MerchantPaymentDetails merchantPaymentDetails) throws RequestCouldNotBeExecutedException {
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