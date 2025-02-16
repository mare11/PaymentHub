package org.sep.bitcoinservice.service;

import org.sep.bitcoinservice.model.Transaction;
import org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus;

import java.util.List;

public interface TransactionService {

    Transaction findByOrderId(String orderId);
    List<Transaction> findByStatus(MerchantOrderStatus merchantOrderStatus);
    Transaction save(Transaction transaction);
}
