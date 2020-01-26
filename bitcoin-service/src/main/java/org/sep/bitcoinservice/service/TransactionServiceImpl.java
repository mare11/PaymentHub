package org.sep.bitcoinservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.bitcoinservice.exceptions.NoTransactionFoundException;
import org.sep.bitcoinservice.model.Transaction;
import org.sep.bitcoinservice.repository.TransactionRepository;
import org.sep.paymentgatewayservice.method.api.MerchantOrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction findByOrderId(String orderId) {
        Transaction transaction = this.transactionRepository.findByOrderId(orderId);
        if (transaction != null){
            return transaction;
        }else{
            log.error("Transaction with order id: {} not found", orderId);
            throw new NoTransactionFoundException(orderId);
        }
    }

    @Override
    public List<Transaction> findByStatus(MerchantOrderStatus merchantOrderStatus) {
        return this.transactionRepository.findAllByStatus(merchantOrderStatus);
    }

    @Override
    public Transaction save(Transaction transaction) {
        if (Stream.of(transaction, transaction.getMerchant(), transaction.getOrderId(), transaction.getPrice(), transaction.getPriceCurrency(), transaction.getStatus())
                .anyMatch(Objects::isNull)) {
            return null;
        }
        return this.transactionRepository.save(transaction);
    }


}
