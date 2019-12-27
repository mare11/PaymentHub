package org.sep.paypalservice.config;

import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.shared.transport.jersey.EurekaJerseyClientImpl;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLContext;

@Configuration
public class PayPalConfig {

    @Value("${server.ssl.trust-store}")
    private Resource trustStore;
    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Bean
    @SneakyThrows
    public DiscoveryClient.DiscoveryClientOptionalArgs discoveryClientOptionalArgs() {
        DiscoveryClient.DiscoveryClientOptionalArgs args = new DiscoveryClient.DiscoveryClientOptionalArgs();
        EurekaJerseyClientImpl.EurekaJerseyClientBuilder builder = new EurekaJerseyClientImpl.EurekaJerseyClientBuilder();
        builder.withClientName("paypal-service");
        builder.withCustomSSL(this.sslContext());
        builder.withMaxTotalConnections(10);
        builder.withMaxConnectionsPerHost(10);
        args.setEurekaJerseyClient(builder.build());
        return args;
    }

    @Bean
    public SSLContext sslContext() throws Exception {
        return new SSLContextBuilder()
                .loadTrustMaterial(this.trustStore.getURL(), this.trustStorePassword.toCharArray(), new TrustSelfSignedStrategy())
                .build();
    }
}