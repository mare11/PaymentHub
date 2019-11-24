package org.sep.paypal;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymentdataservice.api.PaymentDataServiceApi;
import org.sep.paymentdataservice.api.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final PaymentDataServiceApi paymentDataServiceApi;
    private static final String HTTP_PREFIX = "http://";
    private static final String SERVICE_NAME = "PAY-PAL";
    @Value("${spring.application.name}")
    private String SERVICE_HOST;
    @Value("${server.port}")
    private String SERVICE_PORT;

    @Autowired
    public ApplicationStartupListener(final PaymentDataServiceApi paymentDataServiceApi) {
        this.paymentDataServiceApi = paymentDataServiceApi;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent applicationReadyEvent) {
        log.info("Startup event at: {}", applicationReadyEvent.getTimestamp());
        final PaymentMethod method = PaymentMethod.builder().name(SERVICE_NAME)
                .address(HTTP_PREFIX + SERVICE_HOST + ":" + SERVICE_PORT).build();
        paymentDataServiceApi.addPaymentMethod(method);
    }
}
