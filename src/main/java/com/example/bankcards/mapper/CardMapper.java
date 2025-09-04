package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.CardEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", expression = "java(com.example.bankcards.entity.CardStatus.ACTIVE)")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "expMonth", source = "expiry", qualifiedByName = "toMonth")
    @Mapping(target = "expYear",  source = "expiry", qualifiedByName = "toYear")
    @Mapping(target = "balance",  source = "initialBalance")
    @Mapping(target = "last4",    source = "last4")
    CardEntity toEntity(CardCreateRequest req);

    @Mapping(target = "ownerId",      source = "owner.id")
    @Mapping(target = "maskedNumber", source = "last4", qualifiedByName = "mask")
    CardResponse toDto(CardEntity e);

    @Named("toMonth")
    default int toMonth(java.time.YearMonth ym) { return ym.getMonthValue(); }

    @Named("toYear")
    default int toYear(java.time.YearMonth ym) { return ym.getYear(); }

    @Named("mask")
    default String mask(String last4) {
        return (last4 == null || last4.isBlank()) ? "**** **** **** ****" : "**** **** **** " + last4;
    }
}