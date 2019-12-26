package org.sep.bankservice;

import org.sep.paymentgatewayservice.methodapi.PaymentMethodRegistrationApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = PaymentMethodRegistrationApi.class)
public class BankServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(BankServiceApplication.class, args);
    }

}
