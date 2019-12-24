package org.sep.sellerservice.controller;

import org.sep.paymentgatewayservice.api.SellerRegistrationRequest;
import org.sep.paymentgatewayservice.api.SellerRegistrationResponse;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.sellerservice.api.PaymentMethod;
import org.sep.sellerservice.api.SellerServiceApi;
import org.sep.sellerservice.dto.CustomerPaymentDto;
import org.sep.sellerservice.dto.RedirectionDto;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.dto.SellerPaymentMethodsDto;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.service.PaymentService;
import org.sep.sellerservice.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SellerServiceController implements SellerServiceApi {

    private static final String HTTP_PREFIX = "http://";
    @Value("${server.address}")
    private String SERVER_ADDRESS;
    @Value("${frontend-port}")
    private String SERVER_PORT;
    private final SellerService sellerService;
    private final PaymentService paymentService;

    @Autowired
    public SellerServiceController(SellerService sellerService, PaymentService paymentService) {
        this.sellerService = sellerService;
        this.paymentService = paymentService;
    }

    @Override
    public ResponseEntity<SellerRegistrationResponse> registerSeller(SellerRegistrationRequest sellerRegistrationRequest) {
        SellerDto seller = this.sellerService.save(sellerRegistrationRequest);
        SellerRegistrationResponse sellerRegistrationResponse = SellerRegistrationResponse.builder()
                .redirectionUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/seller/" + seller.getId())
                .build();
        return ResponseEntity.ok(sellerRegistrationResponse);
    }

    @Override
    public ResponseEntity<PaymentResponse> preparePayment(PaymentRequest paymentRequest) {
        Long paymentId = this.paymentService.preparePayment(paymentRequest);
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/payment/" + paymentId)
                .build();
        return ResponseEntity.ok(paymentResponse);
    }

    @Override
    public ResponseEntity<Void> enableSeller(String sellerIssn) {
        this.sellerService.enableSeller(sellerIssn);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/methods_chosen")
    public ResponseEntity<RedirectionDto> chooseMethods(@RequestBody SellerPaymentMethodsDto sellerPaymentMethodsDto) {
        return ResponseEntity.ok(RedirectionDto.builder().redirectionUrl(this.sellerService.addPaymentMethods(sellerPaymentMethodsDto)).build());
    }

    @PostMapping(value = "/payment")
    public ResponseEntity<PaymentResponse> proceedPayment(@RequestBody CustomerPaymentDto customerPaymentDto) {
        return ResponseEntity.ok(this.paymentService.proceedPayment(customerPaymentDto));
    }

    @GetMapping(value = "/methods/{id}")
    public ResponseEntity<List<PaymentMethod>> getSellerPaymentMethods(@PathVariable Long id) {
        Payment payment = this.paymentService.findById(id);
        return ResponseEntity.ok(this.sellerService.getSellerPaymentMethods(payment));
    }
}