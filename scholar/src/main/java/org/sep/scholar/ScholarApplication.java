package org.sep.scholar;

import org.sep.paymentclientservice.api.PaymentClientServiceApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = PaymentClientServiceApi.class)
public class ScholarApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ScholarApplication.class, args);
    }

}
