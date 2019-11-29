package org.sep.scholar;

import org.sep.paymentclientservice.api.PaymentClientServiceApi;
import org.sep.paymentdataservice.api.PaymentDataServiceApi;
import org.sep.paymentdataservice.service.PaymentDataService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = {PaymentClientServiceApi.class, PaymentDataServiceApi.class})
public class ScholarApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ScholarApplication.class, args);
    }

}
