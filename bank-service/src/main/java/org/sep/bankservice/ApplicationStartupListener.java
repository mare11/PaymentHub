package org.sep.bankservice;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.method.api.PaymentMethodData;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final String SERVICE_NAME = "Bank";
    @Value("${spring.application.name}")
    private String serviceHost;
    @Value("${server.port}")
    private String servicePort;
    private final PaymentMethodRegistrationApi paymentMethodRegistrationApi;

    @Autowired
    public ApplicationStartupListener(final PaymentMethodRegistrationApi paymentMethodRegistrationApi) {
        this.paymentMethodRegistrationApi = paymentMethodRegistrationApi;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent applicationReadyEvent) {
        log.info("Startup event at: {}", LocalDateTime.ofInstant(Instant.ofEpochMilli(applicationReadyEvent.getTimestamp()), ZoneId.systemDefault()));
        final PaymentMethodData paymentMethodData = PaymentMethodData.builder()
                .name(SERVICE_NAME)
                .serviceName(this.serviceHost)
                .port(Integer.valueOf(this.servicePort))
                .build();
        this.paymentMethodRegistrationApi.registerPaymentMethod(paymentMethodData);
    }
}
