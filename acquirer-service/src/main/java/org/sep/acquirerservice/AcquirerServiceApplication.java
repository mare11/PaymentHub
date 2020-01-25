package org.sep.acquirerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AcquirerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcquirerServiceApplication.class, args);
    }

}
