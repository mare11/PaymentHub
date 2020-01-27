package org.sep.paypalservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.sep.paypalservice.exceptions.RequestCouldNotBeExecutedException;
import org.sep.paypalservice.exceptions.ResourceNotFoundException;
import org.sep.paypalservice.model.MerchantPaymentDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;

@Component
@Slf4j
public class PayPalUtil {

    @Value("${ip.address}")
    public String SERVER_ADDRESS;
    @Value("${frontend-port}")
    public String FRONTEND_PORT;
    public static final String DEFAULT_CURRENCY = "USD";
    public static final String SERBIAN_LOCALE = "en-RS";
    public static final String APPROVE_REL = "approve";
    public static final String PREFER_HEADER = "return=representation";
    public static final String HTTPS_PREFIX = "https://";
    public static final int SCHEDULER_DELAY_IN_SECONDS = 30;
    public static final int SCHEDULER_INITIAL_DELAY_IN_SECONDS = 10;
    private static final String RESOURCE_NOT_FOUND_KEY = "RESOURCE_NOT_FOUND";
    private final SSLContext sslContext;

    @Autowired
    public PayPalUtil(final SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    private PayPalHttpClient getHttpClient(final MerchantPaymentDetails merchantPaymentDetails) {
        final PayPalEnvironment environment = this.getPayPalEnvironment(merchantPaymentDetails.getClientId(), merchantPaymentDetails.getClientSecret());

        final PayPalHttpClient httpClient = new PayPalHttpClient(environment);
        log.info("HttpClient is created...");
        return httpClient;
    }

    public PayPalEnvironment getPayPalEnvironment(final String clientId, final String clientSecret) {
        log.info("Environment is created...");
        return new PayPalEnvironment.Sandbox(clientId, clientSecret);
    }

    public <T> T sendRequest(final HttpRequest<T> request, final MerchantPaymentDetails merchantPaymentDetails)
            throws RequestCouldNotBeExecutedException, ResourceNotFoundException {
        final PayPalHttpClient httpClient = this.getHttpClient(merchantPaymentDetails);
        httpClient.setSSLSocketFactory(this.sslContext.getSocketFactory());

        final T object;
        try {
            log.info("Request is executing...");
            final HttpResponse<T> response = httpClient.execute(request);
            log.info("Response is retrieved...");

            object = response.result();
        } catch (final IOException e) {
            final ObjectMapper objectMapper = new ObjectMapper();
            try {
                final JsonNode jsonMessage = objectMapper.readTree(e.getMessage());
                final String errorKey = jsonMessage.get("name").asText();
                final String errorMessage = jsonMessage.get("message").asText();
                log.error(errorMessage);
                if (errorKey.equals(RESOURCE_NOT_FOUND_KEY)) {
                    throw new ResourceNotFoundException(errorMessage);
                } else {
                    throw new RequestCouldNotBeExecutedException(errorMessage);
                }
            } catch (final JsonProcessingException ex) {
                log.error(ex.getMessage());
                throw new RequestCouldNotBeExecutedException(ex.getMessage());
            }
        }

        return object;
    }
}
