package org.sep.sellerservice;

import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = {PaymentGatewayServiceApi.class})
public class SellerServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(SellerServiceApplication.class, args);
    }

}
