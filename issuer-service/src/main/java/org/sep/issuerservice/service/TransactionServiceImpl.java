package org.sep.issuerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.issuerservice.exception.InvalidDataException;
import org.sep.issuerservice.model.CardEntity;
import org.sep.issuerservice.model.TransactionEntity;
import org.sep.issuerservice.repository.CardRepository;
import org.sep.issuerservice.repository.TransactionRepository;
import org.sep.pccservice.api.PccRequest;
import org.sep.pccservice.api.PccResponse;
import org.sep.pccservice.api.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import static org.sep.pccservice.api.TransactionStatus.SUBMITTED;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(CardRepository cardRepository, TransactionRepository transactionRepository) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionStatus getTransactionStatus(String acquirerOrderId) {
        TransactionEntity transaction = transactionRepository.findByAcquirerOrderId(acquirerOrderId);
        if (transaction == null) {
            log.error("Transaction not found for acquirer order id: {}", acquirerOrderId);
            return null;
        }
        return transaction.getStatus();
    }

    @Override
    public PccResponse handleRequest(PccRequest request) {
        log.info("Handling request: {}", request);
        assertAllNotNull(request, request.getAcquirerOrderId(), request.getAcquirerTimestamp(), request.getAmount(),
                request.getPan(), request.getCcv(), request.getExpirationDate(), request.getCardholderName());

        CardEntity card = cardRepository.findByPanAndCcv(request.getPan(), request.getCcv());
        if (card == null || card.getExpirationDate().isBefore(LocalDate.now())) {
            log.error("Card not found or expired (pan: {})", request.getPan());
            return PccResponse.builder()
                    .success(false)
                    .message("Card not found or expired!")
                    .build();
        }

        if (card.getAvailableAmount() - request.getAmount() < 0) {
            log.error("No enough available money (transactionAmount: {}, availableAmount: {})",
                    request.getAmount(), card.getAvailableAmount());
            return PccResponse.builder()
                    .success(false)
                    .message("No enough available money!")
                    .build();
        }

        log.info("Card found with available amount: {} and reserved amount: {}", card.getAvailableAmount(), card.getReservedAmount());
        card.setAvailableAmount(card.getAvailableAmount() - request.getAmount());
        card.setReservedAmount(card.getReservedAmount() + request.getAmount());
        cardRepository.save(card);
        log.info("Amount: {} transferred from available to reserved amount", request.getAmount());

        TransactionEntity transaction = TransactionEntity.builder()
                .amount(request.getAmount())
                .acquirerOrderId(request.getAcquirerOrderId())
                .acquirerTimestamp(request.getAcquirerTimestamp())
                .card(card)
                .status(SUBMITTED)
                .build();

        transactionRepository.save(transaction);

        return PccResponse.builder()
                .acquirerOrderId(request.getAcquirerOrderId())
                .acquirerTimestamp(request.getAcquirerTimestamp())
                .issuerOrderId(transaction.getId())
                .issuerTimestamp(transaction.getTimestamp())
                .success(true)
                .build();
    }

    private void assertAllNotNull(Object... objects) {
        if (Stream.of(objects).anyMatch(Objects::isNull)) {
            throw new InvalidDataException();
        }
    }
}
