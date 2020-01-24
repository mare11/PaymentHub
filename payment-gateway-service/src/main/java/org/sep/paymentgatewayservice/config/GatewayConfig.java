package org.sep.paymentgatewayservice.config;

import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.shared.transport.jersey.EurekaJerseyClientImpl;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.sep.paymentgatewayservice.method.api.PaymentMethodData;
import org.sep.paymentgatewayservice.seller.api.SellerPaymentMethods;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLContext;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class GatewayConfig {

    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${server.ssl.trust-store}")
    private Resource trustStore;
    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Bean
    public Map<String, PaymentMethodData> paymentMethodDataMap() {
        return new HashMap<>();
    }

    @Bean
    public Map<String, SellerPaymentMethods> sellerRegistrationMap() {
        return new HashMap<>();
    }

    @Bean
    @SneakyThrows
    public DiscoveryClient.DiscoveryClientOptionalArgs discoveryClientOptionalArgs() {
        final DiscoveryClient.DiscoveryClientOptionalArgs args = new DiscoveryClient.DiscoveryClientOptionalArgs();
        final EurekaJerseyClientImpl.EurekaJerseyClientBuilder builder = new EurekaJerseyClientImpl.EurekaJerseyClientBuilder();
        builder.withClientName(this.applicationName);
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