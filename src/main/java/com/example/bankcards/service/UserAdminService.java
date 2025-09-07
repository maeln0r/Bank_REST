package com.example.bankcards.service;

import com.example.bankcards.dto.admin.UserAdminDtos;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.DomainValidationException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.JpaUserRepository;
import com.example.bankcards.repository.spec.UserSpecifications;
import com.example.bankcards.util.PageableUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserAdminService {

    private final JpaUserRepository users;
    private final PasswordEncoder encoder;
    private final UserMapper mapper;

    public UserAdminService(JpaUserRepository users, PasswordEncoder encoder, UserMapper mapper) {
        this.users = users;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    public Page<UserAdminDtos.UserResponse> list(Pageable pageable, String query) {
        var pg = PageableUtils.withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "createdAt"));
        var spec = UserSpecifications.matchesQuery(query);
        return users.findAll(spec, pg).map(mapper::toUserResponse);
    }

    public UserAdminDtos.UserResponse get(UUID id) {
        UserEntity u = users.findById(id)
                .orElseThrow(() -> new NotFoundException("error.user.not_found"));
        return mapper.toUserResponse(u);
    }

    @Transactional
    public UserAdminDtos.UserResponse create(UserAdminDtos.CreateUserRequest req) {
        UserEntity u = mapper.toEntity(req);
        u.setPasswordHash(encoder.encode(req.password()));
        UserEntity saved = users.save(u);
        return mapper.toUserResponse(saved);
    }

    @Transactional
    public UserAdminDtos.UserResponse update(UUID id, UserAdminDtos.UpdateUserRequest req) {
        UserEntity u = users.findById(id)
                .orElseThrow(() -> new NotFoundException("error.user.not_found"));
        mapper.update(req, u);
        return mapper.toUserResponse(u);
    }

    @Transactional
    public void changePassword(UUID id, String newPassword) {
        UserEntity u = users.findById(id)
                .orElseThrow(() -> new NotFoundException("error.user.not_found"));
        if (encoder.matches(newPassword, u.getPasswordHash())) {
            throw new DomainValidationException("error.password.same_as_old", "newPassword");
        }
        u.setPasswordHash(encoder.encode(newPassword));
        users.save(u);
    }

    @Transactional
    public void delete(UUID id) {
        UserEntity u = users.findById(id)
                .orElseThrow(() -> new NotFoundException("error.user.not_found"));
        users.delete(u);
    }
}