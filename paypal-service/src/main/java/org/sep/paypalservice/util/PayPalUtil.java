package org.sep.paypalservice.util;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.sep.paypalservice.exceptions.RequestCouldNotBeExecutedException;
import org.sep.paypalservice.model.MerchantPaymentDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;

@Component
@Slf4j
public class PayPalUtil {

    public static final String SERBIAN_LOCALE = "en-RS";
    public static final String APPROVE_REL = "approve";
    public static final String PREFER_HEADER = "return=representation";
    public static final String HTTPS_PREFIX = "https://";
    private final SSLContext sslContext;

    @Autowired
    public PayPalUtil(final SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    private PayPalHttpClient getHttpClient(final MerchantPaymentDetails merchantPaymentDetails) {
        final PayPalEnvironment environment = new PayPalEnvironment.Sandbox(merchantPaymentDetails.getClientId(), merchantPaymentDetails.getClientSecret());
        log.info("Environment is created...");

        final PayPalHttpClient httpClient = new PayPalHttpClient(environment);
        log.info("HttpClient is created...");
        return httpClient;
    }

    public <T> T sendRequest(final HttpRequest<T> request, final MerchantPaymentDetails merchantPaymentDetails) throws RequestCouldNotBeExecutedException {
        final PayPalHttpClient httpClient = this.getHttpClient(merchantPaymentDetails);
        httpClient.setSSLSocketFactory(this.sslContext.getSocketFactory());

        final T object;
        try {
            log.info("Request is executing...");
            final HttpResponse<T> response = httpClient.execute(request);
            log.info("Response is retrieved...");

            object = response.result();
        } catch (final IOException e) {
            log.error(e.getMessage());
            throw new RequestCouldNotBeExecutedException(e.getMessage());
        }

        return object;
    }
}
