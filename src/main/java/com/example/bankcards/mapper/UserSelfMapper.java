package com.example.bankcards.mapper;

import com.example.bankcards.config.MapStructConfig;
import com.example.bankcards.dto.user.UserSelfDtos;
import com.example.bankcards.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface UserSelfMapper {
    UserSelfDtos.MeResponse toMeResponse(UserEntity u);
}