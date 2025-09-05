package com.example.bankcards.repository.spec;

import com.example.bankcards.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;


public final class UserSpecifications {
    private UserSpecifications() {
    }

    public static Specification<UserEntity> matchesQuery(String query) {
        if (query == null || query.isBlank()) return null; // null -> no filtering
        final String like = "%" + query.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("username")), like),
                cb.like(cb.lower(root.get("email")), like)
        );
    }
}