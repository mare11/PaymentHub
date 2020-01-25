package org.sep.bitcoinservice;

import org.sep.paymentgatewayservice.method.api.PaymentMethodApi;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableEurekaClient
@EnableFeignClients(clients = {PaymentMethodApi.class, PaymentMethodRegistrationApi.class})
public class BitcoinServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(BitcoinServiceApplication.class, args);
    }

}
