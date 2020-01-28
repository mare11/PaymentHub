package org.sep.acquirerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.acquirerservice.model.CardEntity;
import org.sep.acquirerservice.model.TransactionEntity;
import org.sep.acquirerservice.repository.CardRepository;
import org.sep.acquirerservice.repository.TransactionRepository;
import org.sep.pccservice.api.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.sep.pccservice.api.TransactionStatus.*;

@Slf4j
@Service
public class SchedulerService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final PccService pccService;
    private final String acquirerPan;
    private final String expireDuration;

    @Autowired
    public SchedulerService(TransactionRepository transactionRepository, CardRepository cardRepository, PccService pccService,
                            @Value("${acquirer.pan}") String acquirerPan,
                            @Value("${acquirer.transaction.expire-duration}") String expireDuration) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.pccService = pccService;
        this.acquirerPan = acquirerPan;
        this.expireDuration = expireDuration;
    }

    @Scheduled(initialDelayString = "${acquirer.scheduling.initial-delay}",
            fixedDelayString = "${acquirer.scheduling.fixed-delay}")
    private void pickUpRemainingTransactions() {
        checkOpenTransactions();
        executeSubmittedTransactions();
    }

    private void checkOpenTransactions() {
        log.info("Started checking expiration status for transactions at: {}", LocalDateTime.now());
        List<TransactionEntity> openTransactions = transactionRepository.findAllByStatus(OPEN);
        if (openTransactions.isEmpty()) {
            log.info("No open transactions found!");
        }
        openTransactions.forEach(transaction -> {
            if (isTransactionExpired(transaction.getTimestamp())) {
                log.info("Found expired transaction with id: {}", transaction.getId());
                transaction.setStatus(EXPIRED);
                transactionRepository.save(transaction);
                log.info("Changed status of transaction (id: {}) to: {}", transaction.getId(), transaction.getStatus());
            }
        });

        log.info("Finished checking expiration status for transactions at: {}", LocalDateTime.now());
    }

    private boolean isTransactionExpired(LocalDateTime timestamp) {
        return timestamp.plus(Duration.parse(expireDuration)).isBefore(LocalDateTime.now());
    }

    private void executeSubmittedTransactions() {
        log.info("Started executing submitted transactions at: {}", LocalDateTime.now());
        List<TransactionEntity> submittedTransactions = transactionRepository.findAllByStatus(SUBMITTED);
        if (submittedTransactions.isEmpty()) {
            log.info("No submitted transactions found!");
        }
        submittedTransactions.forEach(transaction -> {
            log.info("Found submitted transaction with id: {}", transaction.getId());
            if (!transaction.getCustomerPan().startsWith(acquirerPan)) {
                //ping pcc and issuer to check status
                log.info("Checking transaction status with issuer...");
                TransactionStatus status = pccService.checkTransactionStatus(transaction.getId());
                // TODO check null case (and other statuses) separately
                if (status != null && status != transaction.getStatus()) {
                    if (EXECUTED == status) {
                        CardEntity merchantCard = cardRepository.findByPan(transaction.getMerchantPan());
                        merchantCard.setAvailableAmount(merchantCard.getAvailableAmount() + transaction.getAmount());
                        cardRepository.save(merchantCard);
                    }
                    transaction.setStatus(status);
                    transactionRepository.save(transaction);
                    log.info("Changed status of transaction (id: {}) to: {}", transaction.getId(), status);
                }
            } else { // TODO consider using transactions
                CardEntity customerCard = cardRepository.findByPan(transaction.getCustomerPan());
                customerCard.setReservedAmount(customerCard.getReservedAmount() - transaction.getAmount());
                cardRepository.save(customerCard);

                CardEntity merchantCard = cardRepository.findByPan(transaction.getMerchantPan());
                merchantCard.setAvailableAmount(merchantCard.getAvailableAmount() + transaction.getAmount());
                cardRepository.save(merchantCard);

                transaction.setStatus(EXECUTED);
                transactionRepository.save(transaction);
                log.info("Transaction (id: {}) executed within acquirer!", transaction.getId());
            }
        });
        log.info("Finished executing submitted transactions at: {}", LocalDateTime.now());
    }
}
