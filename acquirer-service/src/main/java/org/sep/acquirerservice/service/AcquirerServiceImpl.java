package org.sep.acquirerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sep.acquirerservice.exception.InvalidTransactionException;
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
        log.info("Transaction request: {}", transactionRequest);
        assertAllNotNull(transactionRequest, transactionRequest.getMerchantId(), transactionRequest.getMerchantPassword(),
                transactionRequest.getAmount(), transactionRequest.getSuccessUrl(), transactionRequest.getErrorUrl());

        Client client = clientRepository.findByMerchantIdAndMerchantPassword(transactionRequest.getMerchantId(), transactionRequest.getMerchantPassword());
        log.info("Client: {}", client);
        if (client == null) {
            throw new InvalidTransactionException();
        }

        String orderId = UUID.randomUUID().toString();
        TransactionEntity transaction = TransactionEntity.builder()
                .amount(transactionRequest.getAmount())
                .successUrl(transactionRequest.getSuccessUrl() + orderId)
                .errorUrl(transactionRequest.getErrorUrl() + orderId)
                .build();

        transaction = transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .paymentId(orderId)
                .paymentUrl(HTTP_PREFIX + SERVER_ADDRESS + ":" + SERVER_PORT + URL_POSTFIX + transaction.getId())
                .build();
    }

    @Override
    public Transaction getTransactionById(String id) {
        return modelMapper.map(getTransactionEntity(id), Transaction.class);
    }

    @Override
    public TransactionResponse executeTransaction(String id, CardDto cardDto) {
        assertAllNotNull(id, cardDto, cardDto.getPan(), cardDto.getCcv(), cardDto.getExpirationDate(), cardDto.getCardholderName());
        TransactionEntity transaction = getTransactionEntity(id);

        Card card = cardRepository.findByPanAndCcvAndExpirationDate(cardDto.getPan(), cardDto.getCcv(), cardDto.getExpirationDate());
        // todo maybe use cancelUrl in case of any check below fails
        if (card == null || card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new InvalidTransactionException();
        }

        List<String> names = getCardholderNames(cardDto.getCardholderName());
        Client client = clientRepository.findByFirstNameAndLastNameAndCards_Pan(names.get(0), names.get(1), cardDto.getPan());
        if (!cardDto.getCardholderName().equals(client.getFirstName() + " " + client.getLastName())) {
            throw new InvalidTransactionException();
        }

        if (card.getAvailableAmount() - transaction.getAmount() < 0) {
            throw new InvalidTransactionException();
        }
        card.setAvailableAmount(card.getAvailableAmount() - transaction.getAmount());
        card.setReservedAmount(card.getReservedAmount() + transaction.getAmount());
        cardRepository.save(card);

        return TransactionResponse.builder()
                .paymentUrl(transaction.getSuccessUrl())
                .build();
    }

    private List<String> getCardholderNames(String fullName) {
        List<String> names = List.of(fullName.split(" "));
        if (names.size() != 2) {
            throw new InvalidTransactionException();
        }
        return names;
    }

    private void assertAllNotNull(Object... objects) {
        if (Stream.of(objects).anyMatch(Objects::isNull)) {
            throw new InvalidTransactionException();
        }
    }

    private TransactionEntity getTransactionEntity(String id) {
        Optional<TransactionEntity> optionalTransaction = transactionRepository.findById(UUID.fromString(id));
        if (optionalTransaction.isEmpty()) {
            throw new TransactionNotFoundException(id);
        }
        return optionalTransaction.get();
    }
}
