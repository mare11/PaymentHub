package org.sep.sellerservice.controller;

import org.sep.paymentgatewayservice.api.PaymentRequest;
import org.sep.paymentgatewayservice.api.PaymentResponse;
import org.sep.sellerservice.api.*;
import org.sep.sellerservice.dto.CustomerPaymentDto;
import org.sep.sellerservice.dto.PaymentDto;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.service.PaymentService;
import org.sep.sellerservice.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
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
        try {
            SellerDto seller = this.sellerService.save(sellerRegistrationRequest);
            SellerRegistrationResponse sellerRegistrationResponse = SellerRegistrationResponse.builder()
                    .redirectionUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/seller/" + seller.getId())
                    .build();
            return ResponseEntity.ok(sellerRegistrationResponse);
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<PaymentResponse> preparePayment(PaymentRequest paymentRequest) {
        try {
            PaymentDto paymentDto = this.paymentService.save(paymentRequest);
            PaymentResponse paymentResponse = PaymentResponse.builder()
                    .paymentId(paymentDto.getId().toString())
                    .redirectionUrl(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/payment/" + paymentDto.getId())
                    .build();
            return ResponseEntity.ok(paymentResponse);
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/methods_chosen")
    public ResponseEntity chooseMethods(@RequestBody SellerPaymentMethods sellerPaymentMethods) {
        SellerDto sellerDto = this.sellerService.addPaymentMethods(sellerPaymentMethods);
        //todo call gateway service and send him selected payment methods
        return ResponseEntity.ok().build();
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