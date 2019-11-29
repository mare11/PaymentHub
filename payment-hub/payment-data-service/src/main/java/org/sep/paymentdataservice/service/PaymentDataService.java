package org.sep.paymentdataservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentdataservice.api.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentDataService {

    private final List<PaymentMethod> paymentMethods = new ArrayList<>();
    private final List<String> paymentClients = new ArrayList<>();

    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethods;
    }

    public Optional<PaymentMethod> getPaymentMethodByName(final String name) {
        return paymentMethods.stream()
                .filter(method -> method.getName().equals(name)).findFirst();
    }

    public void addPaymentMethod(final PaymentMethod paymentMethod) {
        if (paymentMethods.stream()
                .map(PaymentMethod::getName)
                .noneMatch(paymentMethod.getName()::equals)) {
            paymentMethods.add(paymentMethod);
            log.info("Payment method: {} created with address: {}", paymentMethod.getName(), paymentMethod.getAddress());
        }
    }

    public List<String> getPaymentClients() { return paymentClients; }

    public Optional<String> getPaymentClientByName(final String name) {
        return paymentClients.stream()
                .filter(client -> client.equals(name)).findFirst();
    }

    public void addPaymentClient(final String paymentClient) {
        if (paymentClients.stream()
                .noneMatch(paymentClient::equals)) {
            paymentClients.add(paymentClient);
            log.info("Payment client: {} created", paymentClient);
        }
    }
}
