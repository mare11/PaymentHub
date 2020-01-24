package org.sep.sellerservice.controller;

import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.api.SellerRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionResponse;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.paymentgatewayservice.seller.api.SellerServiceApi;
import org.sep.sellerservice.dto.CustomerPaymentDto;
import org.sep.sellerservice.dto.CustomerSubscriptionDto;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.dto.SellerPaymentMethodsDto;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.service.PaymentService;
import org.sep.sellerservice.service.SellerService;
import org.sep.sellerservice.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SellerServiceController implements SellerServiceApi {

    private static final String HTTPS_PREFIX = "https://";
    @Value("${ip.address}")
    private String SERVER_ADDRESS;
    @Value("${frontend-port}")
    private String FRONTEND_PORT;
    private final SellerService sellerService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public SellerServiceController(final SellerService sellerService, final PaymentService paymentService, final SubscriptionService subscriptionService) {
        this.sellerService = sellerService;
        this.paymentService = paymentService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public ResponseEntity<RedirectionResponse> registerSeller(final SellerRequest sellerRegistrationRequest) {
        final SellerDto seller = this.sellerService.save(sellerRegistrationRequest);
        final RedirectionResponse redirectionResponse = RedirectionResponse.builder()
                .redirectionUrl(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/seller/" + seller.getId())
                .build();
        return ResponseEntity.ok(redirectionResponse);
    }

    @Override
    public ResponseEntity<PaymentResponse> preparePayment(final PaymentRequest paymentRequest) {
        final Long paymentId = this.paymentService.preparePayment(paymentRequest);
        final PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentUrl(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/payment/" + paymentId)
                .build();
        return ResponseEntity.ok(paymentResponse);
    }

    @Override
    public ResponseEntity<Void> enableSeller(final String sellerIssn) {
        this.sellerService.enableSeller(sellerIssn);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<RedirectionResponse> prepareSubscription(final SellerRequest sellerRequest) {
        final Long subscriptionId = this.subscriptionService.prepareSubscription(sellerRequest);
        final RedirectionResponse redirectionResponse = RedirectionResponse.builder()
                .redirectionUrl(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.FRONTEND_PORT + "/subscription/" + subscriptionId)
                .build();
        return ResponseEntity.ok(redirectionResponse);
    }

    @PostMapping(value = "/methods_chosen", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedirectionResponse> chooseMethods(@RequestBody final SellerPaymentMethodsDto sellerPaymentMethodsDto) {
        return ResponseEntity.ok(RedirectionResponse.builder().redirectionUrl(this.sellerService.addPaymentMethods(sellerPaymentMethodsDto)).build());
    }

    @PostMapping(value = "/payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResponse> proceedPayment(@RequestBody final CustomerPaymentDto customerPaymentDto) {
        return ResponseEntity.ok(this.paymentService.proceedPayment(customerPaymentDto));
    }

    @GetMapping(value = "/methods/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentMethod>> getSellerPaymentMethods(@PathVariable final Long id) {
        final Payment payment = this.paymentService.findById(id);
        return ResponseEntity.ok(this.sellerService.getSellerPaymentMethods(payment));
    }

    @GetMapping(value = "/subscription/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SubscriptionPlan>> retrieveSubscriptionPlans(@PathVariable final Long id) {
        return ResponseEntity.ok(this.subscriptionService.retrieveSubscriptionPlans(id));
    }

    @PostMapping(value = "/subscription", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody final CustomerSubscriptionDto customerSubscriptionDto) {
        return ResponseEntity.ok(this.subscriptionService.createSubscription(customerSubscriptionDto));
    }
}