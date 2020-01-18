package org.sep.paymentgatewayservice.payment.entity;

import feign.Client;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
public class FeignConfiguration {

    private final SSLContext sslContext;
    private final SpringClientFactory springClientFactory;
    private final CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;

    @Autowired
    public FeignConfiguration(final SSLContext sslContext, final SpringClientFactory springClientFactory, @Qualifier("cachingLBClientFactory") final CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory) {
        this.sslContext = sslContext;
        this.springClientFactory = springClientFactory;
        this.cachingSpringLoadBalancerFactory = cachingSpringLoadBalancerFactory;
    }

    @Bean
    @SneakyThrows
    public Client feignClient() {
        final Client.Default client = new LoadBalancerFeignClient.Default(this.sslContext.getSocketFactory(), new NoopHostnameVerifier());
        return new LoadBalancerFeignClient(client, this.cachingSpringLoadBalancerFactory, this.springClientFactory);
    }
}