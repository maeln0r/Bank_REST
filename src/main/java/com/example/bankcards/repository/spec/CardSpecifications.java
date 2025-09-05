package com.example.bankcards.repository.spec;

import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class CardSpecifications {
    private CardSpecifications() {
    }

    public static Specification<CardEntity> byOwner(UUID ownerId) {
        if (ownerId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<CardEntity> withStatus(CardStatus status) {
        if (status == null) return null;
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<CardEntity> createdFrom(OffsetDateTime from) {
        if (from == null) return null;
        return (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<CardEntity> createdTo(OffsetDateTime to) {
        if (to == null) return null;
        return (root, q, cb) -> cb.lessThan(root.get("createdAt"), to);
    }

    public static Specification<CardEntity> withLast4(String last4) {
        if (last4 == null || last4.isBlank()) return null;
        return (root, q, cb) -> cb.equal(root.get("last4"), last4);
    }

    public static Specification<CardEntity> fromFilter(CardFilter filter, UUID ownerId) {
        Specification<CardEntity> spec = Specification.unrestricted();
        spec = spec.and(byOwner(ownerId));
        if (filter != null) {
            spec = spec.and(withStatus(filter.status()))
                    .and(createdFrom(filter.createdFrom()))
                    .and(createdTo(filter.createdTo()))
                    .and(withLast4(filter.last4()));
        }
        return spec;
    }
}