package org.sep.acquirerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sep.acquirerservice.api.TransactionRequest;
import org.sep.acquirerservice.api.TransactionResponse;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
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

        Client client = clientRepository.findByMerchantId(transactionRequest.getMerchantId());
        log.info("Client: {}", client);
        if (client == null || !client.getMerchantPassword().equals(transactionRequest.getMerchantPassword())) {
            throw new InvalidTransactionException();
        }

        TransactionEntity transaction = TransactionEntity.builder()
                .amount(transactionRequest.getAmount())
                .successUrl(transactionRequest.getSuccessUrl())
                .errorUrl(transactionRequest.getErrorUrl())
                .build();

        transaction = transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .paymentId(new Random().nextLong())
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

        Card card = cardRepository.findByPanAndCcv(cardDto.getPan(), cardDto.getCcv());
        // todo maybe use cancelUrl in case of any check below fails
        if (card == null || card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new InvalidTransactionException();
        }
        if (card.getAvailableAmount() - transaction.getAmount() < 0) {
            throw new InvalidTransactionException();
        }

        // todo validate the cardholder and expiration date in DB

        card.setAvailableAmount(card.getAvailableAmount() - transaction.getAmount());
        card.setReservedAmount(card.getReservedAmount() + transaction.getAmount());
        cardRepository.save(card);

        return TransactionResponse.builder()
                .paymentUrl(transaction.getSuccessUrl())
                .build();
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
