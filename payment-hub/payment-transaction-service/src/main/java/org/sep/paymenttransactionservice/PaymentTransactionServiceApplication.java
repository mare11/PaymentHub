package org.sep.paymenttransactionservice;

import org.sep.paymentdataservice.api.PaymentDataServiceApi;
import org.sep.paymenttransactionservice.api.PaymentMethodApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(clients = {PaymentDataServiceApi.class, PaymentMethodApi.class})
public class PaymentTransactionServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PaymentTransactionServiceApplication.class, args);
    }

}
