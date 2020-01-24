package org.sep.paymentgatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.exceptions.NoPaymentMethodFoundException;
import org.sep.paymentgatewayservice.method.api.PaymentMethodApi;
import org.sep.paymentgatewayservice.method.api.PaymentMethodData;
import org.sep.paymentgatewayservice.method.api.SubscriptionApi;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paymentgatewayservice.seller.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private static final String HTTPS_PREFIX = "https://";
    private final SellerServiceApi sellerServiceApi;
    private final PaymentMethodServiceApi paymentMethodServiceApi;
    private final PaymentMethodApi paymentMethodApi;
    private final SubscriptionApi subscriptionApi;
    private final Map<String, PaymentMethodData> paymentMethodDataMap;
    private final Map<String, SellerPaymentMethods> sellerRegistrationMap;

    @Autowired
    public PaymentGatewayServiceImpl(final SellerServiceApi sellerServiceApi, final PaymentMethodServiceApi paymentMethodServiceApi, final PaymentMethodApi paymentMethodApi, final SubscriptionApi subscriptionApi, final Map<String, PaymentMethodData> paymentMethodDataMap, final Map<String, SellerPaymentMethods> sellerRegistrationMap) {
        this.sellerServiceApi = sellerServiceApi;
        this.paymentMethodServiceApi = paymentMethodServiceApi;
        this.paymentMethodApi = paymentMethodApi;
        this.subscriptionApi = subscriptionApi;
        this.paymentMethodDataMap = paymentMethodDataMap;
        this.sellerRegistrationMap = sellerRegistrationMap;
    }

    @Override
    public PaymentResponse preparePayment(final PaymentRequest paymentRequest) {
        log.info("Call seller service to prepare payment");
        return this.sellerServiceApi.preparePayment(paymentRequest).getBody();
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
        return URI.create(HTTPS_PREFIX + paymentMethodData.getServiceName() + ":" + paymentMethodData.getPort());
    }

    @Override
    public void registerPaymentMethod(final PaymentMethodData paymentMethodData) {
        Assert.notNull(paymentMethodData, "Payment method data object can't be null!");
        Assert.noNullElements(
                Stream.of(paymentMethodData.getServiceName(),
                        paymentMethodData.getPort(),
                        paymentMethodData.getName())
                        .toArray(),
                "One or more fields are not specified.");

        this.paymentMethodDataMap.put(paymentMethodData.getName(), paymentMethodData);

        log.info("Registering payment method with name '{}', service name '{}' and port '{}'...",
                paymentMethodData.getName(),
                paymentMethodData.getServiceName(),
                paymentMethodData.getPort());

        final PaymentMethod paymentMethod = PaymentMethod.builder()
                .name(paymentMethodData.getName())
                .build();

        this.paymentMethodServiceApi.addPaymentMethod(paymentMethod);

        log.info("Successful payment method registration...");
    }

    @Override
    public String registerSellerInPaymentMethod(final SellerPaymentMethods sellerPaymentMethods) throws SellerAlreadyExistsException {
        Assert.notNull(sellerPaymentMethods, "Seller payment methods object can't be null!");
        Assert.noNullElements(
                Stream.of(sellerPaymentMethods.getSellerIssn(),
                        sellerPaymentMethods.getReturnUrl(),
                        sellerPaymentMethods.getPaymentMethods())
                        .toArray(),
                "One or more fields are not specified.");


        this.sellerRegistrationMap.put(sellerPaymentMethods.getSellerIssn(), sellerPaymentMethods);
        log.info("Move on to first (or only) chosen payment method registration...");
        return this.registerSeller(sellerPaymentMethods.getSellerIssn());
    }

    @Override
    public String proceedToNextPaymentMethod(final String sellerIssn) {
        Assert.notNull(sellerIssn, "Seller issn can't be null!");
        log.info("Move on to next payment method or end process if there are no payment methods left...");
        return this.registerSeller(sellerIssn);
    }

    @Override
    public List<SubscriptionPlan> retrieveSubscriptionPlans(final String merchantId) {
        log.info("Calling paypal service to retrieve subscription plans for merchant with id '{}'", merchantId);
        return this.subscriptionApi.retrieveSubscriptionPlans(merchantId).getBody();
    }

    @Override
    public SubscriptionResponse createSubscription(final SubscriptionRequest subscriptionRequest) {
        log.info("Calling paypal service to create subscription for plan with id '{}'", subscriptionRequest.getPlanId());
        return this.subscriptionApi.createSubscription(subscriptionRequest).getBody();
    }

    private String registerSeller(final String sellerIssn) {
        final SellerPaymentMethods sellerPaymentMethods = this.sellerRegistrationMap.get(sellerIssn);

        final String returnUrl;
        if (sellerPaymentMethods.getPaymentMethods().isEmpty()) {
            log.info("No more payment methods left. Go back to seller's site...");
            this.sellerServiceApi.enableSeller(sellerIssn);
            returnUrl = sellerPaymentMethods.getReturnUrl();
        } else {
            final PaymentMethod paymentMethod = sellerPaymentMethods.getPaymentMethods().get(0);

            final PaymentMethodData paymentMethodData = this.paymentMethodDataMap.get(paymentMethod.getName());
            final URI serviceBaseUri = this.generateBaseUri(paymentMethodData);

            log.info("Send registration request for method named '{}'...", paymentMethod.getName());
            returnUrl = this.paymentMethodApi.retrieveSellerRegistrationUrl(serviceBaseUri, sellerIssn).getBody();
            log.info("URL for registration page of method '{}' is retrieved successfully", paymentMethod.getName());

            sellerPaymentMethods.getPaymentMethods().remove(0);
        }

        return returnUrl;
    }
}