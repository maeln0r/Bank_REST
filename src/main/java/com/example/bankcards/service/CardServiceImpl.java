package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.DomainValidationException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.JpaCardRepository;
import com.example.bankcards.repository.JpaUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class CardServiceImpl implements CardService {

    private final JpaCardRepository cardRepo;
    private final JpaUserRepository userRepo;
    private final CardMapper mapper;

    public CardServiceImpl(JpaCardRepository cardRepo, JpaUserRepository userRepo, CardMapper mapper) {
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CardResponse create(CardCreateRequest req) {
        UserEntity owner = userRepo.findById(req.ownerId())
                .orElseThrow(() -> new NotFoundException("User not found: %s".formatted(req.ownerId())));

        if (req.last4() == null || !req.last4().matches("\\d{4}"))
            throw new DomainValidationException("last4 must be 4 digits");

        var entity = mapper.toEntity(req);
        entity.setOwner(owner);
        return mapper.toDto(cardRepo.save(entity));
    }

    @Override
    @Transactional
    public CardResponse activate(UUID cardId) {
        var c = getForUpdate(cardId);
        c.setStatus(CardStatus.ACTIVE);
        return mapper.toDto(c);
    }

    @Override
    @Transactional
    public CardResponse block(UUID cardId) {
        var c = getForUpdate(cardId);
        c.setStatus(CardStatus.BLOCKED);
        return mapper.toDto(c);
    }

    @Override
    @Transactional
    public void delete(UUID cardId) {
        CardEntity c = cardRepo.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found: %s".formatted(cardId)));
        cardRepo.delete(c);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse get(UUID cardId) {
        return mapper.toDto(cardRepo.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found: %s".formatted(cardId))));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> list(CardFilter filter, Pageable pageable) {
        Specification<CardEntity> spec = Specification.unrestricted();
        spec = buildSpec(filter, null, spec);
        return cardRepo.findAll(spec, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> listForOwner(CardFilter filter, Pageable pageable, UUID ownerId) {
        Specification<CardEntity> spec = Specification.unrestricted();
        spec = buildSpec(filter, ownerId, spec);
        return cardRepo.findAll(spec, pageable).map(mapper::toDto);
    }

    private Specification<CardEntity> buildSpec(CardFilter filter, UUID ownerId, Specification<CardEntity> spec) {
        if (ownerId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("owner").get("id"), ownerId));
        }
        if (filter != null) {
            if (filter.status() != null) {
                spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), filter.status()));
            }
            if (filter.createdFrom() != null) {
                spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), filter.createdFrom()));
            }
            if (filter.createdTo() != null) {
                spec = spec.and((root, q, cb) -> cb.lessThan(root.get("createdAt"), filter.createdTo()));
            }
        }
        return spec;
    }

    @Override
    @Transactional
    public void transferBetweenOwnCards(UUID currentUserId, UUID fromId, UUID toId, BigDecimal amount) {
        if (fromId.equals(toId)) {
            throw new DomainValidationException("Source and target cards must differ");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new DomainValidationException("Amount must be > 0");
        }

        CardEntity from = cardRepo.findByIdForUpdate(fromId)
                .orElseThrow(() -> new NotFoundException("Source card not found: %s".formatted(fromId)));
        CardEntity to = cardRepo.findByIdForUpdate(toId)
                .orElseThrow(() -> new NotFoundException("Target card not found: %s".formatted(toId)));

        if (!from.getOwner().getId().equals(currentUserId) || !to.getOwner().getId().equals(currentUserId)) {
            throw new DomainValidationException("Both cards must belong to the current user");
        }
        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new DomainValidationException("Both cards must be ACTIVE");
        }
        YearMonth now = YearMonth.now();
        YearMonth fromExp = YearMonth.of(from.getExpYear(), from.getExpMonth());
        YearMonth toExp = YearMonth.of(to.getExpYear(), to.getExpMonth());
        if (fromExp.isBefore(now) || toExp.isBefore(now)) {
            throw new DomainValidationException("Card expired");
        }
        if (from.getBalance().compareTo(amount) < 0) {
            throw new DomainValidationException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        // thanks to JPA dirty checking, both will be flushed at tx end
    }

    private CardEntity getForUpdate(UUID id) {
        return cardRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Card not found: %s".formatted(id)));
    }
}