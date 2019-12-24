package org.sep.paymentgatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.exceptions.NoPaymentMethodFoundException;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodApi;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodData;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.sellerservice.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private static final String HTTP_PREFIX = "http://";
    private final SellerServiceApi sellerServiceApi;
    private final PaymentMethodServiceApi paymentMethodServiceApi;
    private final PaymentMethodApi paymentMethodApi;
    private final Map<String, PaymentMethodData> paymentMethodDataMap;
    private final Map<String, SellerPaymentMethods> sellerRegistrationMap;

    @Autowired
    public PaymentGatewayServiceImpl(SellerServiceApi sellerServiceApi, PaymentMethodServiceApi paymentMethodServiceApi, PaymentMethodApi paymentMethodApi, Map<String, PaymentMethodData> paymentMethodDataMap, Map<String, SellerPaymentMethods> sellerRegistrationMap) {
        this.sellerServiceApi = sellerServiceApi;
        this.paymentMethodServiceApi = paymentMethodServiceApi;
        this.paymentMethodApi = paymentMethodApi;
        this.paymentMethodDataMap = paymentMethodDataMap;
        this.sellerRegistrationMap = sellerRegistrationMap;
    }

    @Override
    public PaymentResponse preparePayment(PaymentRequest paymentRequest) {
        return this.sellerServiceApi.preparePayment(paymentRequest).getBody();
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) throws NoPaymentMethodFoundException {
        PaymentMethodData paymentMethodData = this.paymentMethodDataMap.get(paymentRequest.getMethod());

        if (paymentMethodData == null) throw new NoPaymentMethodFoundException(paymentRequest.getMethod());

        URI serviceBaseUri = this.generateBaseUri(paymentMethodData);
        return this.paymentMethodApi.createPayment(serviceBaseUri, paymentRequest).getBody();
    }

    private URI generateBaseUri(PaymentMethodData paymentMethodData) {
        return URI.create(HTTP_PREFIX + paymentMethodData.getServiceName() + ":" + paymentMethodData.getPort());
    }

    @Override
    public void registerPaymentMethod(PaymentMethodData paymentMethodData) {
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

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .name(paymentMethodData.getName())
                .build();

        this.paymentMethodServiceApi.addPaymentMethod(paymentMethod);

        log.info("Successful payment method registration...");
    }

    @Override
    public String registerSellerInPaymentMethod(SellerPaymentMethods sellerPaymentMethods) throws SellerAlreadyExistsException {
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
    public String proceedToNextPaymentMethod(String sellerIssn) {
        Assert.notNull(sellerIssn, "Seller issn can't be null!");
        log.info("Move on to next payment method or end process if there are no payment methods left...");
        return this.registerSeller(sellerIssn);
    }

    private String registerSeller(String sellerIssn) {
        SellerPaymentMethods sellerPaymentMethods = this.sellerRegistrationMap.get(sellerIssn);

        String returnUrl = null;
        if (sellerPaymentMethods.getPaymentMethods().isEmpty()) {
            log.info("No more payment methods left. Go back to seller's site...");
            this.sellerServiceApi.enableSeller(sellerIssn);
            returnUrl = sellerPaymentMethods.getReturnUrl();
        } else {
            PaymentMethod paymentMethod = sellerPaymentMethods.getPaymentMethods().get(0);

            PaymentMethodData paymentMethodData = this.paymentMethodDataMap.get(paymentMethod.getName());
            URI serviceBaseUri = this.generateBaseUri(paymentMethodData);

            log.info("Send registration request for method named '{}'...", paymentMethod.getName());
            returnUrl = this.paymentMethodApi.retrieveSellerRegistrationUrl(serviceBaseUri, sellerIssn).getBody();
            log.info("URL for registration page of method '{}' is retrieved successfuly", paymentMethod.getName());

            sellerPaymentMethods.getPaymentMethods().remove(0);
        }

        return returnUrl;
    }
}