package org.sep.paypalservice.service;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.methodapi.*;
import org.sep.paypalservice.enums.TransactionStatus;
import org.sep.paypalservice.exceptions.NoMerchantFoundException;
import org.sep.paypalservice.exceptions.NoOrderFoundException;
import org.sep.paypalservice.exceptions.PaymentCouldNotBeCreatedException;
import org.sep.paypalservice.model.MerchantPaymentDetails;
import org.sep.paypalservice.model.PaymentTransaction;
import org.sep.paypalservice.repository.MerchantPaymentDetailsRepository;
import org.sep.paypalservice.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final String INTENT = "CAPTURE";
    private static final String DEFAULT_CURRENCY = "USD";
    private static final String ITEM_CATEGORY = "DIGITAL_GOODS";
    private static final String SERBIAN_LOCALE = "en-RS";
    private static final String APPROVE_REL = "approve";
    private static final String CAPTURE_REL = "capture";
    private static final String PREFER_HEADER = "return=representation";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final MerchantPaymentDetailsRepository merchantPaymentDetailsRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    public PaymentServiceImpl(MerchantPaymentDetailsRepository merchantPaymentDetailsRepository, PaymentTransactionRepository paymentTransactionRepository) {
        this.merchantPaymentDetailsRepository = merchantPaymentDetailsRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Override
    public PaymentMethodResponse createPayment(PaymentMethodRequest paymentMethodRequest) throws NoMerchantFoundException, PaymentCouldNotBeCreatedException {
        Assert.notNull(paymentMethodRequest, "Payment request can't be null!");
        Assert.noNullElements(
                Stream.of(paymentMethodRequest.getSellerIssn(),
                        paymentMethodRequest.getPrice(),
                        paymentMethodRequest.getSuccessUrl(),
                        paymentMethodRequest.getCancelUrl())
                        .toArray(),
                "One or more fields are not specified.");

        MerchantPaymentDetails merchantPaymentDetails = this.getMerchantPaymentDetails(paymentMethodRequest.getSellerIssn());

        OrdersCreateRequest request = this.createOrderRequest(paymentMethodRequest);
        log.info("Request is created...");

        Order order = this.sendRequest(request, merchantPaymentDetails);

        LinkDescription approveLink = order.links().stream().filter(link -> link.rel().equals(APPROVE_REL)).findFirst().orElse(null);
        log.info("Approve link is retrieved...");

        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .orderId(order.id())
                .status(TransactionStatus.valueOf(order.status()))
                .createdAt(LocalDateTime.parse(order.createTime(), DATE_TIME_FORMATTER))
                .merchantId(paymentMethodRequest.getSellerIssn())
                .item(order.purchaseUnits().get(0).items().get(0).name())
                .value(Double.valueOf(order.purchaseUnits().get(0).amountWithBreakdown().value()))
                .currency(order.purchaseUnits().get(0).amountWithBreakdown().currencyCode())
                .build();

        this.paymentTransactionRepository.save(paymentTransaction);
        log.info("Payment transaction is saved...");

        return PaymentMethodResponse.builder()
                .paymentUrl(approveLink != null ? approveLink.href() : null)
                .orderId(order.id())
                .status(order.status().equals(CreatePaymentStatus.CREATED.name()) ? CreatePaymentStatus.CREATED : CreatePaymentStatus.ERROR)
                .build();
    }

    private OrdersCreateRequest createOrderRequest(PaymentMethodRequest paymentMethodRequest) {

        Money money = new Money()
                .currencyCode(paymentMethodRequest.getPriceCurrency() == null ? DEFAULT_CURRENCY : paymentMethodRequest.getPriceCurrency())
                .value(String.format("%.2f", paymentMethodRequest.getPrice()));

        AmountBreakdown amountBreakdown = new AmountBreakdown().itemTotal(money);

        AmountWithBreakdown amountWithBreakdown = new AmountWithBreakdown()
                .currencyCode(money.currencyCode())
                .value(money.value())
                .amountBreakdown(amountBreakdown);

        Item item = new Item()
                .name(paymentMethodRequest.getItem() == null ? "" : paymentMethodRequest.getItem())
                .category(ITEM_CATEGORY)
                .description(paymentMethodRequest.getDescription() == null ? "" : paymentMethodRequest.getDescription())
                .quantity("1")
                .unitAmount(money);

        List<Item> items = new ArrayList<>();
        items.add(item);

        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .amountWithBreakdown(amountWithBreakdown)
                .items(items);

        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
        purchaseUnitRequests.add(purchaseUnitRequest);

        ApplicationContext applicationContext = new ApplicationContext()
                .brandName(paymentMethodRequest.getSellerName() == null ? "" : paymentMethodRequest.getSellerName())
                .locale(SERBIAN_LOCALE)
                .returnUrl(paymentMethodRequest.getSuccessUrl())
                .cancelUrl(paymentMethodRequest.getCancelUrl());

        OrderRequest orderRequest = new OrderRequest()
                .checkoutPaymentIntent(INTENT)
                .purchaseUnits(purchaseUnitRequests)
                .applicationContext(applicationContext);

        return new OrdersCreateRequest()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .prefer(PREFER_HEADER)
                .requestBody(orderRequest);
    }

    @Override
    public void completePayment(PaymentCompleteRequest paymentCompleteRequest) throws NoOrderFoundException {
        Assert.notNull(paymentCompleteRequest, "Payment complete request can't be null!");
        Assert.noNullElements(
                Stream.of(paymentCompleteRequest.getOrderId(),
                        paymentCompleteRequest.getStatus())
                        .toArray(),
                "One or more fields are not specified.");

        PaymentTransaction paymentTransaction = this.paymentTransactionRepository.findByOrderId(paymentCompleteRequest.getOrderId());

        if (paymentTransaction == null) throw new NoOrderFoundException(paymentCompleteRequest.getOrderId());
        log.info("Transaction is retrieved from DB...");

        if (paymentCompleteRequest.getStatus() == PaymentStatus.SUCCESS) {

            OrdersCaptureRequest request = new OrdersCaptureRequest(paymentCompleteRequest.getOrderId())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .prefer(PREFER_HEADER);
            MerchantPaymentDetails merchantPaymentDetails = this.getMerchantPaymentDetails(paymentTransaction.getMerchantId());
            Order order = this.sendRequest(request, merchantPaymentDetails);

            paymentTransaction.setStatus(TransactionStatus.COMPLETED);
            paymentTransaction.setUpdatedAt(LocalDateTime.parse(order.updateTime(), DATE_TIME_FORMATTER));
            paymentTransaction.setPayerId(order.payer().payerId());

        } else {
            paymentTransaction.setStatus(TransactionStatus.VOIDED);
            paymentTransaction.setUpdatedAt(LocalDateTime.parse(LocalDateTime.now().format(DATE_TIME_FORMATTER), DATE_TIME_FORMATTER));
        }

        this.update(paymentTransaction);
        log.info("Transaction is updated...");
    }

    @Override
    public PaymentTransaction update(PaymentTransaction paymentTransaction) {
        Assert.notNull(paymentTransaction, "Payment transaction can't be null!");

        if (this.paymentTransactionRepository.findById(paymentTransaction.getId()).isEmpty())
            throw new NoOrderFoundException(paymentTransaction.getOrderId());

        return this.paymentTransactionRepository.save(paymentTransaction);
    }

    private PayPalHttpClient getHttpClient(MerchantPaymentDetails merchantPaymentDetails) {
        PayPalEnvironment environment = new PayPalEnvironment.Sandbox(merchantPaymentDetails.getClientId(), merchantPaymentDetails.getClientSecret());
        log.info("Environment is created...");

        PayPalHttpClient httpClient = new PayPalHttpClient(environment);
        log.info("HttpClient is created...");
        return httpClient;
    }

    private MerchantPaymentDetails getMerchantPaymentDetails(String merchantId) {
        MerchantPaymentDetails merchantPaymentDetails = this.merchantPaymentDetailsRepository.findByMerchantId(merchantId);
        log.info("Merchant is retrieved from DB...");

        if (merchantPaymentDetails == null) throw new NoMerchantFoundException(merchantId);
        log.info("Merchant is not null...");
        return merchantPaymentDetails;
    }

    private Order sendRequest(HttpRequest<Order> request, MerchantPaymentDetails merchantPaymentDetails) {
        PayPalHttpClient httpClient = this.getHttpClient(merchantPaymentDetails);

        Order order = null;
        try {
            log.info("Request is executing...");
            HttpResponse<Order> response = httpClient.execute(request);
            log.info("Response is retrieved...");

            order = response.result();
        } catch (IOException e) {
            throw new PaymentCouldNotBeCreatedException(e.getMessage());
        }

        return order;
    }
}