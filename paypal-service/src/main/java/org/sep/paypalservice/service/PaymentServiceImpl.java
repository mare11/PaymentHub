package org.sep.paypalservice.service;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.CreatePaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.enums.TransactionStatus;
import org.sep.paypalservice.exceptions.NoMerchantFoundException;
import org.sep.paypalservice.exceptions.NoOrderFoundException;
import org.sep.paypalservice.exceptions.PaymentCouldNotBeCreatedException;
import org.sep.paypalservice.model.MerchantPaymentDetails;
import org.sep.paypalservice.model.PaymentTransaction;
import org.sep.paypalservice.repository.MerchantPaymentDetailsRepository;
import org.sep.paypalservice.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String PREFER_HEADER = "return=representation";
    private static final String HTTP_PREFIX = "http://";
    @Value("${server.address}")
    private String SERVER_ADDRESS;
    @Value("${server.port}")
    private String SERVER_PORT;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final MerchantPaymentDetailsRepository merchantPaymentDetailsRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    public PaymentServiceImpl(MerchantPaymentDetailsRepository merchantPaymentDetailsRepository, PaymentTransactionRepository paymentTransactionRepository) {
        this.merchantPaymentDetailsRepository = merchantPaymentDetailsRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) throws NoMerchantFoundException, PaymentCouldNotBeCreatedException {
        Assert.notNull(paymentRequest, "Payment request can't be null!");
        Assert.noNullElements(
                Stream.of(paymentRequest.getSellerIssn(),
                        paymentRequest.getPrice(),
                        paymentRequest.getReturnUrl())
                        .toArray(),
                "One or more fields are not specified.");

        MerchantPaymentDetails merchantPaymentDetails = this.getMerchantPaymentDetails(paymentRequest.getSellerIssn());

        OrdersCreateRequest request = this.createOrderRequest(paymentRequest);
        log.info("Request is created...");

        Order order = this.sendRequest(request, merchantPaymentDetails);

        LinkDescription approveLink = order.links().stream().filter(link -> link.rel().equals(APPROVE_REL)).findFirst().orElse(null);
        log.info("Approve link is retrieved...");

        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .orderId(order.id())
                .status(TransactionStatus.valueOf(order.status()))
                .createdAt(LocalDateTime.parse(order.createTime(), DATE_TIME_FORMATTER))
                .merchantId(paymentRequest.getSellerIssn())
                .item(order.purchaseUnits().get(0).items().get(0).name())
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

    private OrdersCreateRequest createOrderRequest(PaymentRequest paymentRequest) {

        Money money = new Money()
                .currencyCode(paymentRequest.getPriceCurrency() == null ? DEFAULT_CURRENCY : paymentRequest.getPriceCurrency())
                .value(String.format("%.2f", paymentRequest.getPrice()));

        AmountBreakdown amountBreakdown = new AmountBreakdown().itemTotal(money);

        AmountWithBreakdown amountWithBreakdown = new AmountWithBreakdown()
                .currencyCode(money.currencyCode())
                .value(money.value())
                .amountBreakdown(amountBreakdown);

        Item item = new Item()
                .name(paymentRequest.getItem() == null ? "" : paymentRequest.getItem())
                .category(ITEM_CATEGORY)
                .description(paymentRequest.getDescription() == null ? "" : paymentRequest.getDescription())
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
                .brandName(paymentRequest.getSellerName() == null ? "" : paymentRequest.getSellerName())
                .locale(SERBIAN_LOCALE)
                .returnUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/success_payment")
                .cancelUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/cancel_payment");

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
    public String completePayment(PaymentCompleteRequest paymentCompleteRequest) throws NoOrderFoundException {
        Assert.notNull(paymentCompleteRequest, "Payment complete request can't be null!");
        Assert.noNullElements(
                Stream.of(paymentCompleteRequest.getOrderId(),
                        paymentCompleteRequest.getStatus())
                        .toArray(),
                "One or more fields are not specified.");

        PaymentTransaction paymentTransaction = this.paymentTransactionRepository.findByOrderId(paymentCompleteRequest.getOrderId());

        if (paymentTransaction == null) throw new NoOrderFoundException(paymentCompleteRequest.getOrderId());
        log.info("Transaction is retrieved from DB...");

        if (paymentTransaction.getStatus() != TransactionStatus.CREATED) return paymentTransaction.getReturnUrl();

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

        paymentTransaction = this.update(paymentTransaction);
        log.info("Transaction is updated...");

        return paymentTransaction.getReturnUrl();
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