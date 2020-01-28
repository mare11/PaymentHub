package org.sep.bankservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.bankservice.model.TransactionEntity;
import org.sep.bankservice.model.TransactionStatus;
import org.sep.bankservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.sep.bankservice.model.TransactionStatus.*;
import static org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus.CANCELED;
import static org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus.*;

@Slf4j
@Service
public class SchedulerService {

    @Value("${acquirer.host}")
    public String acquirerHost;
    @Value("${acquirer.port}")
    public String acquirerPort;
    private static final String HTTPS_PREFIX = "https://";
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public SchedulerService(TransactionRepository transactionRepository, RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(initialDelayString = "${bank.scheduling.initial-delay}",
            fixedDelayString = "${bank.scheduling.fixed-delay}")
    private void checkTransactionsInProgress() {
        log.info("Started checking transactions in progress at: {}", LocalDateTime.now());
        List<TransactionEntity> transactionsInProgress = transactionRepository.findAllByStatus(IN_PROGRESS);
        if (transactionsInProgress.isEmpty()) {
            log.info("No transactions in progress found!");
        }
        transactionsInProgress.forEach(transaction -> {
            log.info("Checking status for transaction (id: {}) with acquirer", transaction.getId());
            try {
                TransactionStatus response = this.restTemplate.getForObject(getAcquirerUrl().concat("/").concat(transaction.getBankTransactionId()), TransactionStatus.class);
                log.info("Got response from acquirer: {}", response);
                if (shouldChangeStatus(response)) {
                    if (EXECUTED == response) {
                        transaction.setStatus(FINISHED);
                    } else { //EXPIRED, CANCELED, FAILED
                        transaction.setStatus(CANCELED);
                    }
                    transactionRepository.save(transaction);
                    log.info("Changed status of transaction (id: {}) to: {}", transaction.getId(), transaction.getStatus());
                }
            } catch (Exception e) {
                log.error("Got exception when calling acquirer: {}", e.getMessage());
            }
        });
        log.info("Finished checking transactions in progress at: {}", LocalDateTime.now());
    }

    private boolean shouldChangeStatus(TransactionStatus response) {
        return response != null && response != OPEN && response != SUBMITTED;
    }

    public String getAcquirerUrl() {
        return HTTPS_PREFIX.concat(acquirerHost).concat(":").concat(acquirerPort).concat("/acquirer");
    }
}
