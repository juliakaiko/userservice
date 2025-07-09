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
     * Конвертирует {@link UserDto} обратно в сущность {@link UserDto}.
     * <p>
     * Реализует <b>обратное преобразование</b> относительно маппинга {@code UserDto -> UserDto}.
     * </p>
     *
     * @param userDto DTO-объект для преобразования (не может быть {@code null})
     * @return соответствующая сущность {@link User}
     */
    @InheritInverseConfiguration
    User toEntity (@NonNull UserDto userDto);
}
