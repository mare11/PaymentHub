package org.sep.bankservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class BankServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(BankServiceApplication.class, args);
    }

}
