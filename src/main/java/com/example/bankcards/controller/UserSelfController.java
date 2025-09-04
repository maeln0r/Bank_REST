package com.example.bankcards.controller;

import com.example.bankcards.dto.user.UserSelfDtos.*;
import com.example.bankcards.service.UserSelfService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserSelfController {
    private final UserSelfService service;

    public UserSelfController(UserSelfService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(service.me());
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UserChangePasswordRequest req) {
        service.changePassword(req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }
}