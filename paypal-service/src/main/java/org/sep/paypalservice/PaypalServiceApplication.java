package org.sep.paypalservice;

import org.sep.paymentgatewayservice.method.api.PaymentMethodApi;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = {PaymentMethodApi.class, PaymentMethodRegistrationApi.class})
public class PaypalServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PaypalServiceApplication.class, args);
    }
}