package org.sep.issuerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.issuerservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchedulerService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public SchedulerService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

//    @Scheduled(initialDelay = 10000, fixedDelay = 30000)
//    public void executeSubmittedTransactions() {
//        log.info("Starting executing submitted transactions at: {}", LocalDateTime.now());
//        List<TransactionEntity> openTransactions = transactionRepository.findAllByStatus(SUBMITTED);
//        if (openTransactions.isEmpty()) {
//            log.info("No submitted transactions found!");
//        }
//        openTransactions.forEach(transaction -> {
//            log.info("Found submitted transaction with id: {}", transaction.getId());
//            TransactionStatus status = pccService.checkTransactionStatus(transaction.getId().toString());
//            if (status != null && status != transaction.getStatus()) {
//                transaction.setStatus(status);
//                transactionRepository.save(transaction);
//                log.info("Changed status of transaction (id: {}) to: {}", transaction.getId(), status);
//            }
//        });
//    }
}
