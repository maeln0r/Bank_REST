package com.example.bankcards.service.scheduler;

import com.example.bankcards.repository.JpaCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Component
public class CardExpiryScheduler {
    private static final Logger log = LoggerFactory.getLogger(CardExpiryScheduler.class);

    private final JpaCardRepository cards;

    public CardExpiryScheduler(JpaCardRepository cards) {
        this.cards = cards;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markExpiredCards() {
        YearMonth now = YearMonth.now();
        int updated = cards.markExpired(now.getYear(), now.getMonthValue());
        if (updated > 0) {
            log.info("Expired cards marked: {}", updated);
        }
    }
}