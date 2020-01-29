package org.sep.sellerservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class Util {

    @Value("${merchant.transaction.expiration-period}")
    private String expirationPeriod;

    public boolean isTransactionExpired(final LocalDateTime timestamp) {
        if (timestamp == null) return true;
        return timestamp.plus(Duration.parse(this.expirationPeriod)).isBefore(LocalDateTime.now());
    }
}
