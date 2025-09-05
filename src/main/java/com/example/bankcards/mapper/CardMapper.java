package com.example.bankcards.mapper;

import com.example.bankcards.config.MapStructConfig;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.mapper.common.CommonMappers;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class, uses = CommonMappers.class)
public interface CardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true) // set in service
    @Mapping(target = "status", expression = "java(com.example.bankcards.entity.CardStatus.ACTIVE)")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "expMonth", source = "expiry", qualifiedByName = "ymToMonth")
    @Mapping(target = "expYear", source = "expiry", qualifiedByName = "ymToYear")
    @Mapping(target = "balance", source = "initialBalance")
    @Mapping(target = "last4", ignore = true)
    CardEntity toEntity(CardCreateRequest req);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "maskedNumber", source = "last4", qualifiedByName = "maskLast4")
    CardResponse toDto(CardEntity e);
}