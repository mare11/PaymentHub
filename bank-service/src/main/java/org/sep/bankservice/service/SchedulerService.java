package org.sep.bankservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchedulerService {

    @Scheduled(initialDelayString = "${bank.scheduling.initial-delay}",
            fixedDelayString = "${bank.scheduling.fixed-delay}")
    private void pickUpRemainingTransactions() {
    }
}
