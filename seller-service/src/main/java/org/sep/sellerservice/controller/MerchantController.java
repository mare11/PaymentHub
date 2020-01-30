package org.sep.sellerservice.controller;

import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paymentgatewayservice.seller.api.MerchantServiceApi;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.sellerservice.dto.*;
import org.sep.sellerservice.model.Merchant;
import org.sep.sellerservice.service.MerchantService;
import org.sep.sellerservice.service.PaymentService;
import org.sep.sellerservice.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MerchantController implements MerchantServiceApi {

    private static final String HTTPS_PREFIX = "https://";
    @Value("${ip.address}")
    private String SERVER_ADDRESS;
    @Value("${frontend-port}")
    private String FRONTEND_PORT;
    private final MerchantService merchantService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public MerchantController(final MerchantService merchantService, final PaymentService paymentService, final SubscriptionService subscriptionService) {
        this.merchantService = merchantService;
        this.paymentService = paymentService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public ResponseEntity<RedirectionResponse> registerMerchant(final MerchantRequest merchantRequest) {
        final Merchant merchant = this.merchantService.save(merchantRequest);
        merchant.setReturnUrl(merchant.getReturnUrl() + '/' + merchant.getId());
        this.merchantService.update(merchant);
        final RedirectionResponse redirectionResponse = RedirectionResponse.builder()
                .redirectionUrl(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/seller/" + merchant.getId())
                .id(merchant.getId())
                .build();
        return ResponseEntity.ok(redirectionResponse);
    }

    @Override
    public ResponseEntity<RegistrationCompleteDto> completeRegistration(String merchantId) {
        Merchant merchant = this.merchantService.findById(merchantId);
        if (merchant == null){
            throw new NullPointerException("Merchant with id: " + merchantId + "is null");
        }
        RegistrationCompleteDto registrationCompleteDto = RegistrationCompleteDto.builder()
                .flag(merchant.getEnabled())
                .build();
        return ResponseEntity.ok(registrationCompleteDto);
    }

    @Override
    public ResponseEntity<RedirectionResponse> preparePayment(final PaymentRequest paymentRequest) {
        final String merchantOrderId = this.paymentService.preparePayment(paymentRequest);
        final RedirectionResponse redirectionResponse = RedirectionResponse.builder()
                .redirectionUrl(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/payment/" + merchantOrderId)
                .id(merchantOrderId)
                .build();
        return ResponseEntity.ok(redirectionResponse);
    }

    @Override
    public ResponseEntity<RedirectionResponse> prepareSubscription(final MerchantRequest merchantRequest) {
        final String subscriptionId = this.subscriptionService.prepareSubscription(merchantRequest);
        final RedirectionResponse redirectionResponse = RedirectionResponse.builder()
                .redirectionUrl(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/subscription/" + subscriptionId)
                .build();
        return ResponseEntity.ok(redirectionResponse);
    }

    @Override
    public ResponseEntity<PaymentMethod> getOrderPaymentMethod(String orderId) {
        return ResponseEntity.ok(this.paymentService.getOrderPaymentMethod(orderId));
    }

    @Override
    public ResponseEntity<Boolean> notifyMerchantIsRegistered(final NotifyPaymentMethodRegistrationDto notifyPaymentMethodRegistrationDto) {
        return ResponseEntity.ok(this.merchantService.notifyMerchantIsRegistered(notifyPaymentMethodRegistrationDto));
    }

    @PostMapping(value = "/methods_chosen", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MerchantRegistrationResponse> chooseMethods(@RequestBody final MerchantPaymentMethodsDto merchantPaymentMethodsDto) {
        return ResponseEntity.ok(this.merchantService.addPaymentMethods(merchantPaymentMethodsDto));
    }

    @PostMapping(value = "/payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResponse> proceedPayment(@RequestBody final CustomerPaymentDto customerPaymentDto) {
        return ResponseEntity.ok(this.paymentService.proceedPayment(customerPaymentDto));
    }

    @GetMapping(value = "/methods/registration/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentMethodDto>> getMerchantPaymentMethodsRegistrationUrls(@PathVariable final String id) {
        return ResponseEntity.ok(this.merchantService.getMerchantPaymentMethodsRegistrationUrls(id));
    }

    @PostMapping(value = "/methods/registration/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MerchantRegistrationResponse> confirmPaymentMethodsRegistration(@RequestBody final String id) {
        return ResponseEntity.ok(this.merchantService.confirmPaymentMethodsRegistration(id));
    }

    @GetMapping(value = "/methods/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentMethod>> getMerchantPaymentMethods(@PathVariable final String id) {
        return ResponseEntity.ok(this.merchantService.getMerchantPaymentMethods(id));
    }

    @GetMapping(value = "/subscription/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SubscriptionPlan>> retrieveSubscriptionPlans(@PathVariable final String id) {
        return ResponseEntity.ok(this.subscriptionService.retrieveSubscriptionPlans(id));
    }

    @PostMapping(value = "/subscription", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody final CustomerSubscriptionDto customerSubscriptionDto) {
        return ResponseEntity.ok(this.subscriptionService.createSubscription(customerSubscriptionDto));
    }
}