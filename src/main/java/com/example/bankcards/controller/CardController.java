package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService service;
    private final CurrentUserService currentUser;

    public CardController(CardService service, CurrentUserService currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse create(@Valid @RequestBody CardCreateRequest req) {
        return service.create(req);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Page<CardResponse> list(@Valid CardFilter filter, Pageable pageable, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            return service.list(filter, pageable);
        } else {
            return service.listForOwner(filter, pageable, currentUser.getCurrentUserId());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public CardResponse get(@PathVariable UUID id, Authentication auth) {
        var dto = service.get(id);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            if (!dto.ownerId().equals(currentUser.getCurrentUserId())) {
                throw new com.example.bankcards.exception.NotFoundException("error.card.not_found");
            }
        }
        return dto;
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse block(@PathVariable UUID id) {
        return service.block(id);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse activate(@PathVariable UUID id) {
        return service.activate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void transfer(@Valid @RequestBody TransferRequest req) {
        service.transferBetweenOwnCards(currentUser.getCurrentUserId(), req.fromCardId(), req.toCardId(), req.amount());
    }

    @PostMapping("/{id}/block-request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void requestBlock(@PathVariable UUID id) {
        service.requestBlock(currentUser.getCurrentUserId(), id);
    }
}