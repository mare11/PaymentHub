package org.sep.issuerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.issuerservice.model.CardEntity;
import org.sep.issuerservice.model.TransactionEntity;
import org.sep.issuerservice.repository.CardRepository;
import org.sep.issuerservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.sep.pccservice.api.TransactionStatus.EXECUTED;
import static org.sep.pccservice.api.TransactionStatus.SUBMITTED;

@Slf4j
@Service
public class SchedulerService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    @Autowired
    public SchedulerService(TransactionRepository transactionRepository, CardRepository cardRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
    }

    @Scheduled(initialDelayString = "${issuer.scheduling.initial-delay}",
            fixedDelayString = "${issuer.scheduling.fixed-delay}")
    public void executeSubmittedTransactions() {
        log.info("Starting executing submitted transactions at: {}", LocalDateTime.now());
        List<TransactionEntity> openTransactions = transactionRepository.findAllByStatus(SUBMITTED);
        if (openTransactions.isEmpty()) {
            log.info("No submitted transactions found!");
        }
        openTransactions.forEach(transaction -> {
            log.info("Found submitted transaction with id: {}", transaction.getId());
            CardEntity card = transaction.getCard();
            card.setReservedAmount(card.getReservedAmount() - transaction.getAmount());
            cardRepository.save(card);

            transaction.setStatus(EXECUTED);
            transactionRepository.save(transaction);
            log.info("Transaction (id: {}) executed!", transaction.getId());
        });
    }
}
