package org.sep.acquirerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sep.acquirerservice.exception.InvalidDataException;
import org.sep.acquirerservice.exception.TransactionNotFoundException;
import org.sep.acquirerservice.model.*;
import org.sep.acquirerservice.repository.CardRepository;
import org.sep.acquirerservice.repository.ClientRepository;
import org.sep.acquirerservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
public class AcquirerServiceImpl implements AcquirerService {

    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final ModelMapper modelMapper;
    private static final String HTTP_PREFIX = "http://";
    private static final String URL_POSTFIX = "/transaction/";
    @Value("${server.address}")
    private String SERVER_ADDRESS;
    @Value("${server.port}")
    private String SERVER_PORT;

    @Autowired

    public AcquirerServiceImpl(ClientRepository clientRepository, TransactionRepository transactionRepository, CardRepository cardRepository) {
        this.clientRepository = clientRepository;
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
            log.error("No card found for merchantId: {}", transactionRequest.getMerchantId());
            throw new InvalidDataException();
        }
        log.info("Card found (pan: {} and ccv: {}) for merchantId: {}", cardEntity.getPan(), cardEntity.getCcv(), transactionRequest.getMerchantId());
        String orderId = UUID.randomUUID().toString();
        TransactionEntity transaction = TransactionEntity.builder()
                .item(transactionRequest.getItem())
                .amount(transactionRequest.getAmount())
                .description(transactionRequest.getDescription())
                .successUrl(transactionRequest.getSuccessUrl() + orderId)
                .errorUrl(transactionRequest.getErrorUrl() + orderId)
                .card(cardEntity)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction (item: {}, price: {}, timestamp: {}, merchantId: {}) saved",
                transaction.getItem(), transaction.getAmount(), transaction.getTimestamp(), transactionRequest.getMerchantId());

        return TransactionResponse.builder()
                .paymentId(orderId)
                .paymentUrl(HTTP_PREFIX + SERVER_ADDRESS + ":" + SERVER_PORT + URL_POSTFIX + transaction.getId())
                .build();
    }

    @Override
    public Transaction getTransactionById(String id) {
        TransactionEntity transactionEntity = getTransactionEntity(id);
        Transaction transaction = modelMapper.map(transactionEntity, Transaction.class);
        transaction.setCardId(transactionEntity.getCard().getId());
        return transaction;
    }

    @Override
    public TransactionResponse executeTransaction(String id, Card card) {
        assertAllNotNull(id, card, card.getPan(), card.getCcv(), card.getExpirationDate(), card.getCardholderName());
        TransactionEntity transaction = getTransactionEntity(id);

        CardEntity customerCard = cardRepository.findByPanAndCcvAndExpirationDate(card.getPan(), card.getCcv(), card.getExpirationDate());
        if (customerCard == null || customerCard.getExpirationDate().isBefore(LocalDate.now())) {
            log.error("Card not found or expired (transactionId: {}, cardId: {})", transaction.getId(), card.getId());
            throw new InvalidDataException();
        }

        List<String> names = getCardholderNames(card.getCardholderName());
        ClientEntity clientEntity = clientRepository.findByFirstNameAndLastNameAndCards_Pan(names.get(0), names.get(1), card.getPan());
        if (!card.getCardholderName().equals(clientEntity.getFirstName() + " " + clientEntity.getLastName())) {
            log.error("Cardholder not valid (cardId: {})", card.getId());
            throw new InvalidDataException();
        }

        if (customerCard.getAvailableAmount() - transaction.getAmount() < 0) {
            log.error("No enough available amount (transactionId: {}, transactionAmount: {}, availableAmount: {})",
                    transaction.getId(), transaction.getAmount(), customerCard.getAvailableAmount());
            return TransactionResponse.builder()
                    .paymentUrl(transaction.getErrorUrl())
                    .build();
        }

        Optional<CardEntity> optionalMerchantCard = cardRepository.findById(card.getId());
        if (optionalMerchantCard.isEmpty()) {
            log.error("Card not found or expired (transactionId: {}, cardId: {})", transaction.getId(), card.getId());
            throw new InvalidDataException();
        }

        log.info("Card found with available amount: {}", customerCard.getAvailableAmount());
        customerCard.setAvailableAmount(customerCard.getAvailableAmount() - transaction.getAmount());
//        customerCard.setReservedAmount(customerCard.getReservedAmount() + transaction.getAmount());
        cardRepository.save(customerCard);
        log.info("Card available amount updated to: {}", customerCard.getAvailableAmount());

        CardEntity merchantCard = optionalMerchantCard.get();
        log.info("Card (pan: {}) found with available amount: {}", merchantCard.getPan(), merchantCard.getAvailableAmount());
        merchantCard.setAvailableAmount(merchantCard.getAvailableAmount() + transaction.getAmount());
        cardRepository.save(merchantCard);
        log.info("Card (pan: {}) available amount updated to: {}", merchantCard.getPan(), merchantCard.getAvailableAmount());

        return TransactionResponse.builder()
                .paymentUrl(transaction.getSuccessUrl())
                .build();
    }

    private List<String> getCardholderNames(String fullName) {
        List<String> names = List.of(fullName.split(" "));
        if (names.size() != 2) {
            throw new InvalidDataException();
        }
        return names;
    }

    private void assertAllNotNull(Object... objects) {
        if (Stream.of(objects).anyMatch(Objects::isNull)) {
            throw new InvalidDataException();
        }
    }

    private TransactionEntity getTransactionEntity(String id) {
        Optional<TransactionEntity> optionalTransaction = transactionRepository.findById(UUID.fromString(id));
        if (optionalTransaction.isEmpty()) {
            log.error("No transaction found with id: {}", id);
            throw new TransactionNotFoundException(id);
        }
        log.info("Transaction found with id: {}", id);
        return optionalTransaction.get();
    }
}
