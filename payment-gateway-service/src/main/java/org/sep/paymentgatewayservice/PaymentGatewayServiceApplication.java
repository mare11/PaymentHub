package org.sep.paymentgatewayservice;

import feign.Contract;
import org.sep.sellerservice.api.SellerRegistrationApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = {SellerRegistrationApi.class})
public class PaymentGatewayServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PaymentGatewayServiceApplication.class, args);
    }

}
