package org.sep.paymentgatewayservice.payment.entity;

import feign.Client;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
public class FeignConfiguration {

    private final SSLContext sslContext;

    @Autowired
    public FeignConfiguration(final SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Bean
    @SneakyThrows
    public Client feignClient() {
        return new Client.Default(this.sslContext.getSocketFactory(), new NoopHostnameVerifier());
    }
}