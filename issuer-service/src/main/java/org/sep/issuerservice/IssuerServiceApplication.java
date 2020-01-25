package org.sep.issuerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IssuerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IssuerServiceApplication.class, args);
    }

}
