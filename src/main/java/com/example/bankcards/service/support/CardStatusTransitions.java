package com.example.bankcards.service.support;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.DomainValidationException;

import java.time.YearMonth;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class CardStatusTransitions {
    private CardStatusTransitions() {
    }

    private static final Map<CardStatus, Set<CardStatus>> ALLOWED = new EnumMap<>(CardStatus.class);

    static {
        ALLOWED.put(CardStatus.ACTIVE, EnumSet.of(CardStatus.PENDING_BLOCK, CardStatus.BLOCKED, CardStatus.EXPIRED));
        ALLOWED.put(CardStatus.PENDING_BLOCK, EnumSet.of(CardStatus.BLOCKED, CardStatus.EXPIRED));
        ALLOWED.put(CardStatus.BLOCKED, EnumSet.of(CardStatus.ACTIVE, CardStatus.EXPIRED));
        ALLOWED.put(CardStatus.EXPIRED, EnumSet.noneOf(CardStatus.class));
    }

    public static boolean isExpired(CardEntity c, YearMonth now) {
        YearMonth expiry = YearMonth.of(c.getExpYear(), c.getExpMonth());
        return expiry.isBefore(now);
    }

    public static void requireTransition(CardEntity c, CardStatus target, YearMonth now) {
        CardStatus from = c.getStatus();
        if (from == target) {
            return;
        }
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(target)) {
            throw new DomainValidationException("error.card.status_illegal", null, new Object[]{from, target});
        }
        if (target == CardStatus.ACTIVE && isExpired(c, now)) {
            throw new DomainValidationException("error.card.cannot_activate_expired");
        }
        if (target == CardStatus.EXPIRED && !isExpired(c, now)) {
            throw new DomainValidationException("error.card.cannot_expire_not_expired");
        }
    }
}