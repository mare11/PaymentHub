package org.sep.acquirerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sep.acquirerservice.exception.InvalidDataException;
import org.sep.acquirerservice.exception.TransactionNotFoundException;
import org.sep.acquirerservice.model.*;
import org.sep.acquirerservice.repository.CardRepository;
import org.sep.acquirerservice.repository.TransactionRepository;
import org.sep.pccservice.api.PccRequest;
import org.sep.pccservice.api.PccResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.sep.pccservice.api.TransactionStatus.OPEN;
import static org.sep.pccservice.api.TransactionStatus.SUBMITTED;

@Slf4j
@Service
public class AcquirerServiceImpl implements AcquirerService {

    private final PccService pccService;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final ModelMapper modelMapper;
    private static final String HTTP_PREFIX = "https://";
    private static final String URL_POSTFIX = "/transaction/";
    @Value("${ip.address}")
    private String serverAddress;
    @Value("${server.port}")
    private String serverPort;
    @Value("${acquirer.pan}")
    private String acquirerPan;

    @Autowired

    public AcquirerServiceImpl(PccService pccService, TransactionRepository transactionRepository, CardRepository cardRepository) {
        this.pccService = pccService;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public TransactionResponse prepareTransaction(TransactionRequest transactionRequest) {
        assertAllNotNull(transactionRequest, transactionRequest.getMerchantId(), transactionRequest.getMerchantPassword(),
                transactionRequest.getAmount(), transactionRequest.getSuccessUrl(), transactionRequest.getErrorUrl());

        CardEntity cardEntity = cardRepository.findByMerchantIdAndMerchantPassword(transactionRequest.getMerchantId(), transactionRequest.getMerchantPassword());
        if (cardEntity == null) {
            log.error("No card found for merchant (id: {})", transactionRequest.getMerchantId());
            return TransactionResponse.builder()
                    .success(false)
                    .message("No card found for merchant with id: " + transactionRequest.getMerchantId())
                    .build();
        }
        log.info("Card found (pan: {} and ccv: {}) for merchantId: {}", cardEntity.getPan(), cardEntity.getCcv(), transactionRequest.getMerchantId());
        TransactionEntity transaction = TransactionEntity.builder()
                .orderId(transactionRequest.getMerchantOrderId())
                .item(transactionRequest.getItem())
                .amount(transactionRequest.getAmount())
                .description(transactionRequest.getDescription())
                .successUrl(transactionRequest.getSuccessUrl())
                .errorUrl(transactionRequest.getErrorUrl())
                .merchantPan(cardEntity.getPan())
                .status(OPEN)
                .build();

        transactionRepository.save(transaction);
        log.info("Transaction (item: {}, price: {}, timestamp: {}, merchantId: {}) saved",
                transaction.getItem(), transaction.getAmount(), transaction.getTimestamp(), transactionRequest.getMerchantId());

        return TransactionResponse.builder()
                .paymentId(transaction.getId())
                .paymentUrl(HTTP_PREFIX + serverAddress + ":" + serverPort + URL_POSTFIX + transaction.getId())
                .success(true)
                .build();
    }

    @Override
    public Transaction getTransactionById(String id) {
        TransactionEntity transactionEntity = getTransactionEntity(id);
        if (OPEN != transactionEntity.getStatus()) {
            throw new InvalidDataException();
        }
        return modelMapper.map(transactionEntity, Transaction.class);
    }

    @Override
    public TransactionResponse submitTransaction(String id, Card card) {
        assertAllNotNull(id, card, card.getPan(), card.getCcv(), card.getExpirationDate(), card.getCardholderName());
        TransactionEntity transaction = getTransactionEntity(id);

        if (OPEN != transaction.getStatus()) {
            throw new InvalidDataException();
        }

        if (!card.getPan().startsWith(acquirerPan)) {
            log.info("Card number is not from the acquirer bank!");
            PccResponse response = pccService.sendRequestToPcc(
                    PccRequest.builder()
                            .acquirerOrderId(id)
                            .acquirerTimestamp(transaction.getTimestamp())
                            .amount(transaction.getAmount())
                            .pan(card.getPan())
                            .ccv(card.getCcv())
                            .expirationDate(card.getExpirationDate())
                            .cardholderName(card.getCardholderName())
                            .build());

            if (!response.isSuccess()) {
                return TransactionResponse.builder()
                        .paymentUrl(transaction.getErrorUrl())
                        .build();
            }

        } else {
            //customer is from the same bank as merchant
            CardEntity customerCard = cardRepository.findByPanAndCcv(card.getPan(), card.getCcv());
            if (customerCard == null || customerCard.getExpirationDate().isBefore(LocalDate.now())) {
                log.error("Card not found or expired (transactionId: {}, pan: {})", transaction.getId(), card.getPan());
                throw new InvalidDataException();
            }

            if (customerCard.getAvailableAmount() - transaction.getAmount() < 0) {
                log.error("No enough available money (transactionId: {}, transactionAmount: {}, availableAmount: {})",
                        transaction.getId(), transaction.getAmount(), customerCard.getAvailableAmount());
                return TransactionResponse.builder()
                        .paymentUrl(transaction.getErrorUrl())
                        .build();
            }

            log.info("Card found with available amount: {} and reserved amount: {}", customerCard.getAvailableAmount(), customerCard.getReservedAmount());
            customerCard.setAvailableAmount(customerCard.getAvailableAmount() - transaction.getAmount());
            customerCard.setReservedAmount(customerCard.getReservedAmount() + transaction.getAmount());
            cardRepository.save(customerCard);
            log.info("Amount: {} transferred from available to reserved amount", transaction.getAmount());
        }

        transaction.setStatus(SUBMITTED);
        transaction.setCustomerPan(card.getPan());
        transactionRepository.save(transaction);
        log.info("Transaction: {} submitted!", transaction.getId());

        return TransactionResponse.builder()
                .paymentUrl(transaction.getSuccessUrl())
                .build();
    }

    private void assertAllNotNull(Object... objects) {
        if (Stream.of(objects).anyMatch(Objects::isNull)) {
            throw new InvalidDataException();
        }
    }

    private TransactionEntity getTransactionEntity(String id) {
        Optional<TransactionEntity> optionalTransaction = transactionRepository.findById(id);
        if (optionalTransaction.isEmpty()) {
            log.error("No transaction found with id: {}", id);
            throw new TransactionNotFoundException(id);
        }
        log.info("Transaction found with id: {}", id);
        return optionalTransaction.get();
    }
}
