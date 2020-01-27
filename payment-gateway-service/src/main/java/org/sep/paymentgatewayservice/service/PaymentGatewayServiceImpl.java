package org.sep.paymentgatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.exceptions.NoPaymentMethodFoundException;
import org.sep.paymentgatewayservice.method.api.MerchantOrderStatus;
import org.sep.paymentgatewayservice.method.api.PaymentMethodApi;
import org.sep.paymentgatewayservice.method.api.PaymentMethodData;
import org.sep.paymentgatewayservice.method.api.SubscriptionApi;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paymentgatewayservice.seller.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private static final String HTTPS_PREFIX = "https://";
    private final MerchantServiceApi merchantServiceApi;
    private final PaymentMethodServiceApi paymentMethodServiceApi;
    private final PaymentMethodApi paymentMethodApi;
    private final SubscriptionApi subscriptionApi;
    private final Map<String, PaymentMethodData> paymentMethodDataMap;
    private final Map<String, MerchantPaymentMethods> merchantPaymentMethodsMap;

    @Autowired
    public PaymentGatewayServiceImpl(final MerchantServiceApi merchantServiceApi, final PaymentMethodServiceApi paymentMethodServiceApi, final PaymentMethodApi paymentMethodApi, final SubscriptionApi subscriptionApi, final Map<String, PaymentMethodData> paymentMethodDataMap, final Map<String, MerchantPaymentMethods> merchantPaymentMethodsMap) {
        this.merchantServiceApi = merchantServiceApi;
        this.paymentMethodServiceApi = paymentMethodServiceApi;
        this.paymentMethodApi = paymentMethodApi;
        this.subscriptionApi = subscriptionApi;
        this.paymentMethodDataMap = paymentMethodDataMap;
        this.merchantPaymentMethodsMap = merchantPaymentMethodsMap;
    }

    @Override
    public RedirectionResponse preparePayment(final PaymentRequest paymentRequest) {
        log.info("Call seller service to prepare payment");
        return this.merchantServiceApi.preparePayment(paymentRequest).getBody();
    }

    @Override
    public PaymentResponse createPayment(final PaymentRequest paymentRequest) throws NoPaymentMethodFoundException {
        final PaymentMethodData paymentMethodData = this.paymentMethodDataMap.get(paymentRequest.getMethod());

        if (paymentMethodData == null) {
            log.error("Payment method not found");
            throw new NoPaymentMethodFoundException(paymentRequest.getMethod());
        }

        final URI serviceBaseUri = this.generateBaseUri(paymentMethodData);

        log.info("Payment request sent to {} payment method from gateway", paymentMethodData.getName());
        return this.paymentMethodApi.createPayment(serviceBaseUri, paymentRequest).getBody();
    }

    private URI generateBaseUri(final PaymentMethodData paymentMethodData) {
        return URI.create(HTTPS_PREFIX + paymentMethodData.getHost() + ":" + paymentMethodData.getPort());
    }

    @Override
    public void registerPaymentMethod(final PaymentMethodData paymentMethodData) {
        Assert.notNull(paymentMethodData, "Payment method data object can't be null!");
        Assert.noNullElements(
                Stream.of(paymentMethodData.getHost(),
                        paymentMethodData.getPort(),
                        paymentMethodData.getName())
                        .toArray(),
                "One or more fields are not specified.");

        this.paymentMethodDataMap.put(paymentMethodData.getName(), paymentMethodData);

        log.info("Registering payment method with name '{}', service name '{}' and port '{}'...",
                paymentMethodData.getName(),
                paymentMethodData.getHost(),
                paymentMethodData.getPort());

        final PaymentMethod paymentMethod = PaymentMethod.builder()
                .name(paymentMethodData.getName())
                .build();

        this.paymentMethodServiceApi.addPaymentMethod(paymentMethod);

        log.info("Successful payment method registration...");
    }

    @Override
    public Map<Long, String> retrievePaymentMethodsRegistrationUrl(final MerchantPaymentMethods merchantPaymentMethods) throws MerchantAlreadyExistsException {
        Assert.notNull(merchantPaymentMethods, "Merchant payment methods object can't be null!");
        Assert.noNullElements(
                Stream.of(merchantPaymentMethods.getMerchantId(),
                        merchantPaymentMethods.getReturnUrl(),
                        merchantPaymentMethods.getPaymentMethods())
                        .toArray(),
                "One or more fields are not specified.");


        final Map<Long, String> registrationUrlsMap = new HashMap<>();
        merchantPaymentMethods.getPaymentMethods().forEach(paymentMethod -> {
            final PaymentMethodData paymentMethodData = this.paymentMethodDataMap.get(paymentMethod.getName());
            final URI serviceBaseUri = this.generateBaseUri(paymentMethodData);
            log.info("Send request to retrieve merchant registration url for method named '{}'...", paymentMethod.getName());
            final String registrationUrl = this.paymentMethodApi.retrieveMerchantRegistrationUrl(serviceBaseUri, merchantPaymentMethods.getMerchantId()).getBody();
            log.info("URL for registration page of method '{}' is retrieved successfully", paymentMethod.getName());

            if (registrationUrl != null) {
                registrationUrlsMap.put(paymentMethod.getId(), registrationUrl);
            }
        });

        return registrationUrlsMap;
    }

    @Override
    public Boolean notifyMerchantIsRegistered(final NotifyPaymentMethodRegistrationDto notifyPaymentMethodRegistrationDto) {
        Assert.notNull(notifyPaymentMethodRegistrationDto, "Notify payment method registration object can't be null!");
        Assert.notNull(notifyPaymentMethodRegistrationDto.getMethodName(), "Payment method name can't be null!");
        Assert.notNull(notifyPaymentMethodRegistrationDto.getMerchantId(), "Merchant id can't be null!");

        if (this.paymentMethodDataMap.get(notifyPaymentMethodRegistrationDto.getMethodName()) == null) {
            log.error("Payment method with name '{}' does not exist", notifyPaymentMethodRegistrationDto.getMethodName());
            return false;
        }

        log.info("Notify seller service that merchant with id '{}' is registered on payment service named '{}'",
                notifyPaymentMethodRegistrationDto.getMerchantId(),
                notifyPaymentMethodRegistrationDto.getMethodName());
        final Boolean successFlag = this.merchantServiceApi.notifyMerchantIsRegistered(notifyPaymentMethodRegistrationDto).getBody();

        log.info("Seller service response flag for merchant with id '{}' registration on payment service named '{}': {}",
                notifyPaymentMethodRegistrationDto.getMerchantId(),
                notifyPaymentMethodRegistrationDto.getMethodName(),
                successFlag);

        return successFlag;
    }

    @Override
    public List<SubscriptionPlan> retrieveSubscriptionPlans(final String merchantId) {
        log.info("Calling paypal service to retrieve subscription plans for merchant with id '{}'", merchantId);
        return this.subscriptionApi.retrieveSubscriptionPlans(merchantId).getBody();
    }

    @Override
    public SubscriptionResponse createSubscription(final SubscriptionRequest subscriptionRequest) {
        log.info("Calling paypal service to create subscription for plan with id '{}'", subscriptionRequest.getId());
        return this.subscriptionApi.createSubscription(subscriptionRequest).getBody();
    }

    @Override
    public SubscriptionCancelResponse cancelSubscription(final SubscriptionCancelRequest subscriptionCancelRequest) {
        log.info("Calling paypal service to cancel subscription with merchant subscription id '{}'", subscriptionCancelRequest.getMerchantSubscriptionId());
        return this.subscriptionApi.cancelSubscription(subscriptionCancelRequest).getBody();
    }

    @Override
    public MerchantOrderStatus checkOrderStatus(final String orderId) {
        Assert.notNull(orderId, "Order id can't be null!");
        log.info("Order (id: {}) payment method request sent to seller service", orderId);
        PaymentMethod paymentMethod = this.merchantServiceApi.getOrderPaymentMethod(orderId).getBody();
        if (paymentMethod == null){
            log.warn("Payment method hasn't been set yet");
            return MerchantOrderStatus.IN_PROGRESS;
        }

        final PaymentMethodData paymentMethodData = this.paymentMethodDataMap.get(paymentMethod.getName());

        if (paymentMethodData == null) {
            log.error("Payment method not found");
            throw new NoPaymentMethodFoundException(paymentMethod.getName());
        }

        final URI serviceBaseUri = this.generateBaseUri(paymentMethodData);
        log.info("Check order status request sent to {} payment method from gateway", paymentMethodData.getName());
        return this.paymentMethodApi.getOrderStatus(serviceBaseUri, orderId).getBody();
    }
}