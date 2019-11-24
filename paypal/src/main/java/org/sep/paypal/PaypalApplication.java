package org.sep.paypal;

import org.sep.paymentdataservice.api.PaymentDataServiceApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = {PaymentDataServiceApi.class})
public class PaypalApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PaypalApplication.class, args);
    }

}
