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
import com.example.bankcards.repository.spec.CardSpecifications;
import com.example.bankcards.service.support.CardStatusTransitions;
import com.example.bankcards.util.PageableUtils;
import com.example.bankcards.config.MoneyProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class CardServiceImpl implements CardService {

    private final JpaCardRepository cardRepo;
    private final JpaUserRepository userRepo;
    private final CardMapper mapper;
    private final MoneyProperties money;

    public CardServiceImpl(JpaCardRepository cardRepo, JpaUserRepository userRepo, CardMapper mapper, MoneyProperties money) {
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
        this.money = money;
    }

    @Override
    @Transactional
    public CardResponse create(CardCreateRequest req) {
        UserEntity owner = userRepo.findById(req.ownerId())
                .orElseThrow(() -> new NotFoundException("User not found: %s".formatted(req.ownerId())));

        String pan = normalizePan(req.pan());
        String last4 = pan.substring(pan.length() - 4);

        BigDecimal initial = normalizeInitialBalance(req.initialBalance());

        var entity = mapper.toEntity(req);
        entity.setOwner(owner);
        entity.setLast4(last4);
        entity.setBalance(initial);
        return mapper.toDto(cardRepo.save(entity));
    }

    @Override
    @Transactional
    public CardResponse activate(UUID cardId) {
        var c = getForUpdate(cardId);
        CardStatusTransitions.requireTransition(c, CardStatus.ACTIVE, YearMonth.now());
        c.setStatus(CardStatus.ACTIVE);
        return mapper.toDto(c);
    }

    @Override
    @Transactional
    public CardResponse block(UUID cardId) {
        var c = getForUpdate(cardId);
        if (c.getStatus() == CardStatus.BLOCKED) {
            return mapper.toDto(c);
        }
        if (c.getStatus() == CardStatus.EXPIRED) {
            throw new DomainValidationException("Cannot block an expired card");
        }
        CardStatusTransitions.requireTransition(c, CardStatus.BLOCKED, YearMonth.now());
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
        var spec = CardSpecifications.fromFilter(filter, null);
        var pg = PageableUtils.withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "createdAt"));
        return cardRepo.findAll(spec, pg).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> listForOwner(CardFilter filter, Pageable pageable, UUID ownerId) {
        var spec = CardSpecifications.fromFilter(filter, ownerId);
        var pg = PageableUtils.withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "createdAt"));
        return cardRepo.findAll(spec, pg).map(mapper::toDto);
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

        BigDecimal normalizedAmount = normalizeTransferAmount(amount);

        UUID firstId = fromId.compareTo(toId) <= 0 ? fromId : toId;
        UUID secondId = fromId.compareTo(toId) <= 0 ? toId : fromId;
        CardEntity first = cardRepo.findByIdForUpdate(firstId)
                .orElseThrow(() -> new NotFoundException("Card not found: %s".formatted(firstId)));
        CardEntity second = cardRepo.findByIdForUpdate(secondId)
                .orElseThrow(() -> new NotFoundException("Card not found: %s".formatted(secondId)));
        CardEntity from = first.getId().equals(fromId) ? first : second;
        CardEntity to = from == first ? second : first;

        if (!from.getOwner().getId().equals(currentUserId) || !to.getOwner().getId().equals(currentUserId)) {
            throw new DomainValidationException("Both cards must belong to the current user");
        }
        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new DomainValidationException("Both cards must be ACTIVE");
        }
        YearMonth now = YearMonth.now();
        if (CardStatusTransitions.isExpired(from, now) || CardStatusTransitions.isExpired(to, now)) {
            throw new DomainValidationException("Card expired");
        }

        applyTransferBalances(from, to, normalizedAmount);
    }

    @Override
    @Transactional
    public void requestBlock(UUID currentUserId, UUID cardId) {
        CardEntity c = cardRepo.findByIdForUpdate(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found: %s".formatted(cardId)));
        if (!c.getOwner().getId().equals(currentUserId)) {
            throw new DomainValidationException("Only owner can request block");
        }
        if (c.getStatus() == CardStatus.PENDING_BLOCK) {
            return;
        }
        YearMonth now = YearMonth.now();
        if (CardStatusTransitions.isExpired(c, now)) {
            throw new DomainValidationException("Cannot request block for expired card");
        }
        CardStatusTransitions.requireTransition(c, CardStatus.PENDING_BLOCK, now);
        c.setStatus(CardStatus.PENDING_BLOCK);
    }

    private CardEntity getForUpdate(UUID id) {
        return cardRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Card not found: %s".formatted(id)));
    }

    private BigDecimal normalizeInitialBalance(BigDecimal raw) {
        int SCALE = money.getScale();
        RoundingMode RM = money.getRoundingMode();
        BigDecimal MAX_BALANCE = money.getMaxBalance();

        BigDecimal v = (raw == null ? BigDecimal.ZERO : raw);
        if (v.scale() > SCALE) {
            throw new DomainValidationException("Initial balance must have at most %d decimal places".formatted(SCALE));
        }
        v = v.setScale(SCALE, RM);
        if (v.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Initial balance must be >= 0");
        }
        if (v.compareTo(MAX_BALANCE) > 0) {
            throw new DomainValidationException("Initial balance exceeds system limit");
        }
        return v;
    }

    private BigDecimal normalizeTransferAmount(BigDecimal raw) {
        int SCALE = money.getScale();
        RoundingMode RM = money.getRoundingMode();
        BigDecimal MAX_AMOUNT = money.getMaxAmount();

        if (raw.scale() > SCALE) {
            throw new DomainValidationException("Amount must have at most %d decimal places".formatted(SCALE));
        }
        BigDecimal v = raw.setScale(SCALE, RM);
        if (v.compareTo(MAX_AMOUNT) > 0) {
            throw new DomainValidationException("Amount exceeds system limit");
        }
        return v;
    }

    private void applyTransferBalances(CardEntity from, CardEntity to, BigDecimal normalizedAmount) {
        int SCALE = money.getScale();
        RoundingMode RM = money.getRoundingMode();
        BigDecimal MAX_BALANCE = money.getMaxBalance();

        BigDecimal fromBal = from.getBalance().setScale(SCALE, RM);
        BigDecimal toBal = to.getBalance().setScale(SCALE, RM);
        BigDecimal fromAfter = fromBal.subtract(normalizedAmount);
        if (fromAfter.signum() < 0) {
            throw new DomainValidationException("Insufficient funds");
        }
        BigDecimal toAfter = toBal.add(normalizedAmount);
        if (toAfter.compareTo(MAX_BALANCE) > 0) {
            throw new DomainValidationException("Target balance would exceed system limit");
        }

        from.setBalance(fromAfter);
        to.setBalance(toAfter);
    }

    private static String normalizePan(String raw) {
        if (raw == null) throw new DomainValidationException("PAN is required");
        String digits = raw.replaceAll("\\D", "");
        int len = digits.length();
        if (len < 13 || len > 19) {
            throw new DomainValidationException("PAN must be 13–19 digits");
        }
        if (!luhnValid(digits)) {
            throw new DomainValidationException("PAN failed Luhn check");
        }
        return digits;
    }

    private static boolean luhnValid(String digits) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}