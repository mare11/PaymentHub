package org.sep.pccservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.pccservice.api.PccRequest;
import org.sep.pccservice.api.PccResponse;
import org.sep.pccservice.exception.InvalidDataException;
import org.sep.pccservice.model.Transaction;
import org.sep.pccservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final RestTemplate restTemplate;
    private final TransactionRepository transactionRepository;
    private static final String HTTPS_PREFIX = "https://";
    @Value("${issuer.host}")
    private String issuerHost;
    @Value("${issuer.port}")
    private String issuerPort;

    @Autowired
    public TransactionServiceImpl(RestTemplate restTemplate, TransactionRepository transactionRepository) {
        this.restTemplate = restTemplate;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public PccResponse forwardRequest(PccRequest request) {
        log.info("Forwarding from acquirer to issuer for request: {}", request);
        assertAllNotNull(request, request.getAcquirerOrderId(), request.getAcquirerTimestamp(), request.getAmount(),
                request.getPan(), request.getCcv(), request.getExpirationDate(), request.getCardholderName());

        Transaction transaction = Transaction.builder()
                .acquirerOrderId(request.getAcquirerOrderId())
                .acquirerTimestamp(request.getAcquirerTimestamp())
                .amount(request.getAmount())
                .pan(request.getPan())
                .ccv(request.getCcv())
                .expirationDate(request.getExpirationDate())
                .cardholderName(request.getCardholderName())
                .build();

        transactionRepository.save(transaction);

        HttpEntity<PccRequest> requestEntity = new HttpEntity<>(request);
        ResponseEntity<PccResponse> responseEntity = this.restTemplate.exchange(getUrl(), HttpMethod.POST, requestEntity, PccResponse.class);
        PccResponse response = responseEntity.getBody();
        log.info("Got response from the issuer: {}", response);
        return response;
    }

    private void assertAllNotNull(Object... objects) {
        if (Stream.of(objects).anyMatch(Objects::isNull)) {
            throw new InvalidDataException();
        }
    }

    private String getUrl() {
        return HTTPS_PREFIX + issuerHost + ":" + issuerPort;
    }
}
