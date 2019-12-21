package org.sep.paymentgatewayservice;

import org.sep.sellerservice.api.SellerServiceApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = {SellerServiceApi.class})
public class PaymentGatewayServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PaymentGatewayServiceApplication.class, args);
    }

}
