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

import java.time.LocalDateTime;
import java.util.List;

import static org.sep.pccservice.api.TransactionStatus.ACCEPTED;
import static org.sep.pccservice.api.TransactionStatus.SUBMITTED;

@Slf4j
@Service
public class SchedulerService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final PccService pccService;
    private final String acquirerPan;

    @Autowired
    public SchedulerService(TransactionRepository transactionRepository, CardRepository cardRepository, PccService pccService, @Value("${acquirer.pan}") String acquirerPan) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.pccService = pccService;
        this.acquirerPan = acquirerPan;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 30000)
    public void executeSubmittedTransactions() {
        log.info("Starting executing submitted transactions at: {}", LocalDateTime.now());
        List<TransactionEntity> openTransactions = transactionRepository.findAllByStatus(SUBMITTED);
        if (openTransactions.isEmpty()) {
            log.info("No submitted transactions found!");
        }
        openTransactions.forEach(transaction -> {
            log.info("Found submitted transaction with id: {}", transaction.getId());
            if (!transaction.getCustomerPan().startsWith(acquirerPan)) {
                //ping pcc and issuer to check status
                log.info("Checking transaction status with issuer...");
                TransactionStatus status = pccService.checkTransactionStatus(transaction.getId());
                if (status != null && status != transaction.getStatus()) {
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

                transaction.setStatus(ACCEPTED);
                transactionRepository.save(transaction);
                log.info("Transaction (id: {}) executed within acquirer!", transaction.getId());
            }
        });
    }
}
