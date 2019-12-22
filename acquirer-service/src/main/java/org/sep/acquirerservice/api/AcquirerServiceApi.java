package org.sep.acquirerservice.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("acquirer-service")
public interface AcquirerServiceApi {

    @PostMapping(value = "/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    TransactionResponse prepareTransaction(@RequestBody TransactionRequest transactionRequest);
}
