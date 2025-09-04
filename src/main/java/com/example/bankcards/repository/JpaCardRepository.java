package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaCardRepository extends JpaRepository<CardEntity, UUID>, JpaSpecificationExecutor<CardEntity> {

    Page<CardEntity> findAllByOwner_Id(UUID ownerId, Pageable pageable);

    Page<CardEntity> findAllByOwner_IdAndStatus(UUID ownerId, CardStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CardEntity c where c.id = :id")
    Optional<CardEntity> findByIdForUpdate(@Param("id") UUID id);
}