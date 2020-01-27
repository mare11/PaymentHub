package org.sep.sellerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.sellerservice.dto.CustomerPaymentDto;
import org.sep.sellerservice.exceptions.MerchantIsNotEnabledException;
import org.sep.sellerservice.exceptions.NoMerchantFoundException;
import org.sep.sellerservice.exceptions.NoPaymentFoundException;
import org.sep.sellerservice.model.Merchant;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.model.PaymentMethodEntity;
import org.sep.sellerservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MerchantService merchantService;
    private final PaymentGatewayServiceApi paymentGatewayServiceApi;

    @Autowired
    public PaymentServiceImpl(final PaymentRepository paymentRepository, final MerchantService merchantService, final PaymentGatewayServiceApi paymentGatewayServiceApi) {
        this.paymentRepository = paymentRepository;
        this.merchantService = merchantService;
        this.paymentGatewayServiceApi = paymentGatewayServiceApi;
    }

    @Override
    public Payment findById(final String id) {
        return this.paymentRepository.findById(id).orElse(null);
    }

    @Override
    public String preparePayment(final PaymentRequest paymentRequest) throws NoMerchantFoundException {
        Assert.notNull(paymentRequest, "Payment request can't be null!");
        Assert.noNullElements(
                Stream.of(paymentRequest.getPrice(),
                        paymentRequest.getMerchantId(),
                        paymentRequest.getReturnUrl())
                        .toArray(),
                "One or more fields are not specified.");

        final Merchant merchant = this.merchantService.findById(paymentRequest.getMerchantId());
        log.info("Merchant with merchant id: {} is retrieved", paymentRequest.getMerchantId());

        if (merchant == null || !merchant.getEnabled()) {
            log.error("Merchant is not found or is not enabled");
            throw new NoMerchantFoundException(paymentRequest.getMerchantId());
        }

        final Payment payment = Payment.builder()
                .merchant(merchant)
                .item(paymentRequest.getItem())
                .description(paymentRequest.getDescription())
                .price(paymentRequest.getPrice())
                .returnUrl(paymentRequest.getReturnUrl())
                .build();
        this.paymentRepository.save(payment);
        log.info("Payment (merchant: {}, merchant order id: {}) is created and saved in seller", payment.getMerchant().getId(), payment.getId());
        return payment.getId();
    }

    @Override
    public PaymentResponse proceedPayment(final CustomerPaymentDto customerPaymentDto) throws MerchantIsNotEnabledException, NoPaymentFoundException {
        Assert.notNull(customerPaymentDto, "Customer payment can't be null!");
        Assert.noNullElements(
                Stream.of(customerPaymentDto.getMerchantOrderId(),
                        customerPaymentDto.getPaymentMethod())
                        .toArray(),
                "One or more fields are not specified.");
        Assert.noNullElements(
                Stream.of(customerPaymentDto.getPaymentMethod().getId(),
                        customerPaymentDto.getPaymentMethod().getName())
                        .toArray(),
                "One or more fields in payment method are not specified.");

        final Payment payment = this.findById(customerPaymentDto.getMerchantOrderId());

        if (payment == null) {
            log.error("Payment is not found");
            throw new NoPaymentFoundException(customerPaymentDto.getMerchantOrderId());
        }

        if (!payment.getMerchant().getEnabled()) {
            log.error("Merchant is not enabled");
            throw new MerchantIsNotEnabledException(payment.getMerchant().getId());
        }

        payment.setPaymentMethodEntity(PaymentMethodEntity.builder()
                .id(customerPaymentDto.getPaymentMethod().getId())
                .name(customerPaymentDto.getPaymentMethod().getName())
                .build());

        this.paymentRepository.save(payment);

        final PaymentRequest paymentRequest = PaymentRequest.builder()
                .item(payment.getItem())
                .description(payment.getDescription())
                .price(payment.getPrice())
                .method(customerPaymentDto.getPaymentMethod().getName())
                .merchantId(payment.getMerchant().getId())
                .merchantName(payment.getMerchant().getName())
                .merchantOrderId(payment.getId())
                .returnUrl(payment.getReturnUrl())
                .build();
        log.info("Payment request sent to payment-gateway-service from seller-service");
        return this.paymentGatewayServiceApi.createPayment(paymentRequest).getBody();
    }

    @Override
    public PaymentMethod getOrderPaymentMethod(String orderId) {
        log.info("Retrieve payment with order id: {}", orderId);
        Optional<Payment> payment = this.paymentRepository.findById(orderId);
        if (payment.isEmpty()){
            log.error("Payment with order id: {} not found", orderId);
            throw new NoPaymentFoundException(orderId);
        }
        PaymentMethodEntity paymentMethodEntity = payment.get().getPaymentMethodEntity();
        if (paymentMethodEntity == null){
            log.warn("Payment method hasn't been set yet");
            return null;
        }

        log.info("Order payment method: {} sent back to gateway", paymentMethodEntity.getName());
        return PaymentMethod.builder()
                .id(paymentMethodEntity.getId())
                .name(paymentMethodEntity.getName())
                .build();
    }
}