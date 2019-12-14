package org.sep.paymentgatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class PaymentGatewayServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PaymentGatewayServiceApplication.class, args);
    }

}
