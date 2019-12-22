package org.sep.sellerservice.service;

import org.modelmapper.ModelMapper;
import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.api.PaymentRequest;
import org.sep.paymentgatewayservice.api.PaymentResponse;
import org.sep.sellerservice.dto.CustomerPaymentDto;
import org.sep.sellerservice.dto.PaymentDto;
import org.sep.sellerservice.exceptions.NoSellerFoundException;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.model.Seller;
import org.sep.sellerservice.repository.PaymentRepository;
import org.sep.sellerservice.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.stream.Stream;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SellerRepository sellerRepository;
    private final PaymentGatewayServiceApi paymentGatewayServiceApi;
    private final ModelMapper modelMapper = new ModelMapper();

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
    public PaymentDto save(PaymentRequest paymentRequest) throws NoSellerFoundException {
        Assert.notNull(paymentRequest, "Payment request can't be null!");
        Assert.noNullElements(
                Stream.of(paymentRequest.getItem(),
                        paymentRequest.getAmount(),
                        paymentRequest.getPrice(),
                        paymentRequest.getSellerIssn())
                        .toArray(),
                "One or more fields are not specified.");

        Seller seller = this.sellerRepository.findByIssn(paymentRequest.getSellerIssn());

        if (seller == null || !seller.getEnabled())
            throw new NoSellerFoundException(paymentRequest.getSellerIssn().toString());

        Payment payment = Payment.builder()
                .seller(seller)
                .item(paymentRequest.getItem())
                .amount(paymentRequest.getAmount())
                .price(paymentRequest.getPrice())
                .build();
        payment = this.paymentRepository.save(payment);
        return this.modelMapper.map(payment, PaymentDto.class);
    }

    @Override
    public PaymentResponse proceedPayment(CustomerPaymentDto customerPaymentDto) {
        Assert.notNull(customerPaymentDto, "Customer payment can't be null!");
        Assert.noNullElements(
                Stream.of(customerPaymentDto.getPaymentId(),
                        customerPaymentDto.getPaymentMethod(),
                        customerPaymentDto.getPaymentMethod().getId(),
                        customerPaymentDto.getPaymentMethod().getName())
                        .toArray(),
                "One or more fields are not specified.");

        Payment payment = this.findById(customerPaymentDto.getPaymentId());

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .item(payment.getItem())
                .amount(payment.getAmount())
                .price(payment.getPrice())
                .method(customerPaymentDto.getPaymentMethod().getName())
                .sellerIssn(payment.getSeller().getIssn())
                .build();
        return this.paymentGatewayServiceApi.createPayment(paymentRequest);
    }
}