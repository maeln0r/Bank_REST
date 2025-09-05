package com.example.bankcards.mapper;

import com.example.bankcards.config.MapStructConfig;
import com.example.bankcards.entity.RoleType;
import com.example.bankcards.entity.UserEntity;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.Set;

import static com.example.bankcards.dto.admin.UserAdminDtos.*;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    UserResponse toUserResponse(UserEntity u);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(UpdateUserRequest req, @MappingTarget UserEntity u);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    UserEntity toEntity(CreateUserRequest req);

    @AfterMapping
    default void afterCreate(CreateUserRequest req, @MappingTarget UserEntity u) {
        Boolean enabled = req.enabled();
        u.setEnabled(enabled == null || enabled);
        Set<RoleType> roles = req.roles();
        if (roles == null || roles.isEmpty()) {
            u.setRoles(new HashSet<>(Set.of(RoleType.ROLE_USER)));
        } else {
            u.setRoles(new HashSet<>(roles));
        }
    }
}