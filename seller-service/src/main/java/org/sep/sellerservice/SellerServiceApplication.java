package org.sep.sellerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class SellerServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(SellerServiceApplication.class, args);
    }

}
