package com.mymicroservice.userservice.mapper;

import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.model.User;
import lombok.NonNull;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANSE = Mappers.getMapper (UserMapper.class);

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "surname", source = "user.surname")
    @Mapping(target = "birthDate", source = "user.birthDate")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "password", source = "user.password")
    @Mapping(target = "role", source = "user.role")
    UserDto toDto(User user);

    /**
     * Converts {@link UserDto} back to {@link User} entity.
     * <p>
     * Implements <b>reverse mapping</b> relative to {@code User -> UserDto} conversion.
     * </p>
     *
     * @param userDto DTO object to convert (cannot be {@code null})
     * @return corresponding {@link User} entity
     */
    @InheritInverseConfiguration
    User toEntity (@NonNull UserDto userDto);
}
