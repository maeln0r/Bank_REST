package com.example.bankcards.controller;

import com.example.bankcards.dto.admin.UserAdminDtos.ChangePasswordRequest;
import com.example.bankcards.dto.admin.UserAdminDtos.CreateUserRequest;
import com.example.bankcards.dto.admin.UserAdminDtos.UpdateUserRequest;
import com.example.bankcards.dto.admin.UserAdminDtos.UserResponse;
import com.example.bankcards.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserAdminService service;

    public AdminUserController(UserAdminService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return new ResponseEntity<>(service.create(req), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id, @Valid @RequestBody ChangePasswordRequest req) {
        service.changePassword(id, req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}