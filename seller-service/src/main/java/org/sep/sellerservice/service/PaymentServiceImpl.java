package org.sep.sellerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.sellerservice.dto.CustomerPaymentDto;
import org.sep.sellerservice.exceptions.NoPaymentFoundException;
import org.sep.sellerservice.exceptions.NoSellerFoundException;
import org.sep.sellerservice.exceptions.SellerIsNotEnabledException;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.model.Seller;
import org.sep.sellerservice.repository.PaymentRepository;
import org.sep.sellerservice.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.stream.Stream;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SellerRepository sellerRepository;
    private final PaymentGatewayServiceApi paymentGatewayServiceApi;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, SellerRepository sellerRepository, PaymentGatewayServiceApi paymentGatewayServiceApi) {
        this.paymentRepository = paymentRepository;
        this.sellerRepository = sellerRepository;
        this.paymentGatewayServiceApi = paymentGatewayServiceApi;
    }

    @Override
    public Payment findById(Long id) {
        return this.paymentRepository.findById(id).orElse(null);
    }

    @Override
    public Long preparePayment(PaymentRequest paymentRequest) throws NoSellerFoundException {
        Assert.notNull(paymentRequest, "Payment request can't be null!");
        Assert.noNullElements(
                Stream.of(paymentRequest.getPrice(),
                        paymentRequest.getSellerIssn(),
                        paymentRequest.getReturnUrl())
                        .toArray(),
                "One or more fields are not specified.");

        Seller seller = this.sellerRepository.findByIssn(paymentRequest.getSellerIssn());
        log.info("Seller with issn: {} is retrieved", paymentRequest.getSellerIssn());

        if (seller == null || !seller.getEnabled()) {
            log.error("Seller is not found or is not enabled");
            throw new NoSellerFoundException(paymentRequest.getSellerIssn());
        }

        Payment payment = Payment.builder()
                .seller(seller)
                .item(paymentRequest.getItem())
                .description(paymentRequest.getDescription())
                .price(paymentRequest.getPrice())
                .priceCurrency(paymentRequest.getPriceCurrency())
                .returnUrl(paymentRequest.getReturnUrl())
                .build();
        payment = this.paymentRepository.save(payment);
        log.info("Payment (merchant: {}, item: {}) is created and saved in seller", payment.getSeller().getIssn(), payment.getItem());
        return payment.getId();
    }

    @Override
    public PaymentResponse proceedPayment(CustomerPaymentDto customerPaymentDto) throws SellerIsNotEnabledException, NoPaymentFoundException {
        Assert.notNull(customerPaymentDto, "Customer payment can't be null!");
        Assert.noNullElements(
                Stream.of(customerPaymentDto.getPaymentId(),
                        customerPaymentDto.getPaymentMethod())
                        .toArray(),
                "One or more fields are not specified.");
        Assert.noNullElements(
                Stream.of(customerPaymentDto.getPaymentMethod().getId(),
                        customerPaymentDto.getPaymentMethod().getName())
                        .toArray(),
                "One or more fields in payment method are not specified.");
        Payment payment = this.findById(customerPaymentDto.getPaymentId());

        if (payment == null){
            log.error("Payment is not found");
            throw new NoPaymentFoundException(customerPaymentDto.getPaymentId());
        }

        if (!payment.getSeller().getEnabled()) {
            log.error("Seller is not enabled");
            throw new SellerIsNotEnabledException(payment.getSeller().getIssn());
        }

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .item(payment.getItem())
                .description(payment.getDescription())
                .price(payment.getPrice())
                .method(customerPaymentDto.getPaymentMethod().getName())
                .sellerIssn(payment.getSeller().getIssn())
                .sellerName(payment.getSeller().getName())
                .returnUrl(payment.getReturnUrl())
                .build();
        log.info("Payment request sent to gateway from seller");
        return this.paymentGatewayServiceApi.createPayment(paymentRequest).getBody();
    }
}