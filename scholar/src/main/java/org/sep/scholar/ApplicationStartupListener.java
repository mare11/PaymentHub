package org.sep.scholar;

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
    private static final String SERVICE_NAME = "SCHOLAR";

    @Autowired
    public ApplicationStartupListener(final PaymentDataServiceApi paymentDataServiceApi) {
        this.paymentDataServiceApi = paymentDataServiceApi;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent applicationReadyEvent) {
        log.info("Startup event at: {}", applicationReadyEvent.getTimestamp());
        paymentDataServiceApi.addPaymentClient(SERVICE_NAME);
    }
}
