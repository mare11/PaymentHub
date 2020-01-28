package org.sep.acquirerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.pccservice.api.PccRequest;
import org.sep.pccservice.api.PccResponse;
import org.sep.pccservice.api.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class PccService {

    private final RestTemplate restTemplate;
    private static final String HTTPS_PREFIX = "https://";
    @Value("${pcc.host}")
    private String pccHost;
    @Value("${pcc.port}")
    private String pccPort;

    @Autowired
    public PccService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PccResponse sendRequestToPcc(PccRequest pccRequest) {
        log.info("Sending request to PCC. Request: {}", pccRequest);
        HttpEntity<PccRequest> requestEntity = new HttpEntity<>(pccRequest);
        try {
            ResponseEntity<PccResponse> responseEntity = this.restTemplate.exchange(getPccUrl(), HttpMethod.POST, requestEntity, PccResponse.class);
            PccResponse response = responseEntity.getBody();
            log.info("Got response from PCC. Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Got exception when calling acquirer: {}", e.getMessage());
            return PccResponse.builder().build();
        }
    }

    public TransactionStatus checkTransactionStatus(String transactionId) {
        log.info("Checking status for transaction (id: {})", transactionId);
        try {
            TransactionStatus response = this.restTemplate.getForObject(getPccUrl().concat("/").concat(transactionId), TransactionStatus.class);
            log.info("Got response from PCC. Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Got exception when calling acquirer: {}", e.getMessage());
            return null;
        }
    }

    private String getPccUrl() {
        return HTTPS_PREFIX + pccHost + ":" + pccPort;
    }
}
