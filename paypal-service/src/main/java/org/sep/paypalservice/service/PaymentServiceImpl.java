package org.sep.paypalservice.service;

import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.method.api.MerchantOrderStatus;
import org.sep.paymentgatewayservice.payment.entity.CreatePaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.dto.CompleteDto;
import org.sep.paypalservice.dto.RedirectionDto;
import org.sep.paypalservice.exceptions.NoMerchantFoundException;
import org.sep.paypalservice.exceptions.NoOrderFoundException;
import org.sep.paypalservice.exceptions.RequestCouldNotBeExecutedException;
import org.sep.paypalservice.exceptions.ResourceNotFoundException;
import org.sep.paypalservice.model.MerchantPaymentDetails;
import org.sep.paypalservice.model.PaymentTransaction;
import org.sep.paypalservice.model.TransactionStatus;
import org.sep.paypalservice.repository.PaymentTransactionRepository;
import org.sep.paypalservice.util.PayPalUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String INTENT = "CAPTURE";
    private static final String ITEM_CATEGORY = "DIGITAL_GOODS";
    private final MerchantPaymentDetailsService merchantPaymentDetailsService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PayPalUtil payPalUtil;

    @Autowired
    public PaymentServiceImpl(final MerchantPaymentDetailsService merchantPaymentDetailsService, final PaymentTransactionRepository paymentTransactionRepository, final PayPalUtil payPalUtil) {
        this.merchantPaymentDetailsService = merchantPaymentDetailsService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.payPalUtil = payPalUtil;
    }

    @Override
    public PaymentResponse createPayment(final PaymentRequest paymentRequest) throws NoMerchantFoundException, RequestCouldNotBeExecutedException {
        Assert.notNull(paymentRequest, "Payment request can't be null!");
        Assert.noNullElements(
                Stream.of(paymentRequest.getMerchantId(),
                        paymentRequest.getPrice(),
                        paymentRequest.getReturnUrl())
                        .toArray(),
                "One or more fields are not specified.");

        final MerchantPaymentDetails merchantPaymentDetails = this.merchantPaymentDetailsService.findByMerchantId(paymentRequest.getMerchantId());

        final OrdersCreateRequest request = this.createOrderRequest(paymentRequest);
        log.info("Request is created...");

        final Order order = this.payPalUtil.sendRequest(request, merchantPaymentDetails);

        final LinkDescription approveLink = order.links().stream().filter(link -> link.rel().equals(PayPalUtil.APPROVE_REL)).findFirst().orElse(null);
        log.info("Approve link is retrieved...");

        final PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .orderId(order.id())
                .status(TransactionStatus.valueOf(order.status()))
                .merchantId(paymentRequest.getMerchantId())
                .merchantOrderId(paymentRequest.getMerchantOrderId())
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
                .status(order.status().equals(CreatePaymentStatus.CREATED.name()) ? CreatePaymentStatus.CREATED : CreatePaymentStatus.ERROR)
                .build();
    }

    private OrdersCreateRequest createOrderRequest(final PaymentRequest paymentRequest) {

        final Money money = new Money()
                .currencyCode(PayPalUtil.DEFAULT_CURRENCY)
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
                .brandName(paymentRequest.getMerchantName() == null ? "" : paymentRequest.getMerchantName())
                .locale(PayPalUtil.SERBIAN_LOCALE)
                .returnUrl(PayPalUtil.HTTPS_PREFIX + this.payPalUtil.SERVER_ADDRESS + ":" + this.payPalUtil.FRONTEND_PORT + "/success_payment")
                .cancelUrl(PayPalUtil.HTTPS_PREFIX + this.payPalUtil.SERVER_ADDRESS + ":" + this.payPalUtil.FRONTEND_PORT + "/cancel_payment");

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
            final OrdersCaptureRequest captureRequest = new OrdersCaptureRequest(paymentTransaction.getOrderId()).prefer(PayPalUtil.PREFER_HEADER);
            log.info("Send capture request for payment transaction with order id '{}'", paymentTransaction.getOrderId());
            final Order order = this.payPalUtil.sendRequest(captureRequest, this.merchantPaymentDetailsService.findByMerchantId(paymentTransaction.getMerchantId()));

            paymentTransaction.setPayerId(order.payer() != null ? order.payer().payerId() : null);
            paymentTransaction.setPayerName(order.payer() != null && order.payer().name() != null
                    ? order.payer().name().givenName().concat(" ").concat(order.payer().name().surname())
                    : null);
            this.updateTransactionStatus(paymentTransaction, TransactionStatus.COMPLETED);

            log.info("Payment transaction with id '{}' is completed successfully", completeDto.getId());
        }

        return RedirectionDto.builder().redirectionUrl(paymentTransaction.getReturnUrl()).build();
    }

    @Override
    public MerchantOrderStatus getOrderStatus(final String merchantOrderId) {
        Assert.notNull(merchantOrderId, "Merchant order id can't be null!");

        log.info("Retrieving status of payment transaction with merchant order id '{}'", merchantOrderId);
        final PaymentTransaction paymentTransaction = this.paymentTransactionRepository.findByMerchantOrderId(merchantOrderId);

        if (paymentTransaction == null) {
            log.error("Payment transaction with merchant order id '{}' does not exist", merchantOrderId);
            throw new NoOrderFoundException(merchantOrderId);
        }

        log.info("Status of payment transaction with merchant order id '{}' is retrieved successfully. Status value: {}",
                merchantOrderId,
                paymentTransaction.getStatus().name());

        return paymentTransaction.getStatus().equals(TransactionStatus.COMPLETED) ? MerchantOrderStatus.FINISHED
                : paymentTransaction.getStatus().equals(TransactionStatus.VOIDED) ? MerchantOrderStatus.CANCELED : MerchantOrderStatus.IN_PROGRESS;
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
            } catch (final ResourceNotFoundException e) {
                log.info("Order with id {} could not be found", paymentTransaction.getOrderId());
                this.updateTransactionStatus(paymentTransaction, TransactionStatus.VOIDED);
            } catch (final RequestCouldNotBeExecutedException e) {
                log.info("Request to PayPal API for payment with id {} has failed because of communication problems", paymentTransaction.getOrderId());
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
            } catch (final ResourceNotFoundException e) {
                log.info("Order with id {} could not be found", paymentTransaction.getOrderId());
                this.updateTransactionStatus(paymentTransaction, TransactionStatus.VOIDED);
            } catch (final RequestCouldNotBeExecutedException e) {
                log.info("Request to PayPal API for payment with id {} has failed because of communication problems", paymentTransaction.getOrderId());
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
}