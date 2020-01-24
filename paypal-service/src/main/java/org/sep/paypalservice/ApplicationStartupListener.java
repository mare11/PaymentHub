package org.sep.paypalservice;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentgatewayservice.method.api.PaymentMethodData;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@EnableScheduling
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final String SERVICE_NAME = "PayPal";
    @Value("${spring.application.name}")
    private String SERVICE_HOST;
    @Value("${server.port}")
    private String SERVICE_PORT;
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
                .serviceName(this.SERVICE_HOST)
                .port(Integer.valueOf(this.SERVICE_PORT))
                .build();
        this.paymentMethodRegistrationApi.registerPaymentMethod(paymentMethodData);
    }
}