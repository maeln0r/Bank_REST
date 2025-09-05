package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardService {
    CardResponse create(CardCreateRequest req);

    CardResponse activate(UUID cardId);

    CardResponse block(UUID cardId);

    void delete(UUID cardId);

    CardResponse get(UUID cardId);

    Page<CardResponse> list(CardFilter filter, Pageable pageable);

    Page<CardResponse> listForOwner(CardFilter filter, Pageable pageable, UUID ownerId);

    void transferBetweenOwnCards(UUID currentUserId, UUID fromId, UUID toId, BigDecimal amount);

    void requestBlock(UUID currentUserId, UUID cardId);
}