package org.sep.paymentdataservice.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@FeignClient("data-service")
public interface PaymentDataServiceApi {

    @GetMapping(value = "/method", produces = MediaType.APPLICATION_JSON_VALUE)
    List<PaymentMethod> getAllPaymentMethods();

    @GetMapping(value = "/method/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    Optional<PaymentMethod> getPaymentMethodByName(@PathVariable String name);

    @PostMapping(value = "/method", consumes = MediaType.APPLICATION_JSON_VALUE)
    void addPaymentMethod(@RequestBody PaymentMethod paymentMethod);

    @GetMapping(value = "/client", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> getAllPaymentClients();

    @GetMapping(value = "/client/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    Optional<String> getPaymentClientByName(@PathVariable String name);

    @PostMapping(value = "/client", consumes = MediaType.APPLICATION_JSON_VALUE)
    void addPaymentClient(@RequestBody String paymentClient);
}
