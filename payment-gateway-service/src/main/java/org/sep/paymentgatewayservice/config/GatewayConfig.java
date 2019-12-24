package org.sep.paymentgatewayservice.config;

import org.sep.paymentgatewayservice.methodapi.PaymentMethodData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GatewayConfig {

    @Bean
    public Map<String, PaymentMethodData> paymentMethodDataMap() {
        return new HashMap<>();
    }
}