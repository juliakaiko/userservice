package com.mymicroservice.userservice.mapper;

import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.model.CardInfo;
import lombok.NonNull;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CardInfoMapper {

    CardInfoMapper INSTANSE = Mappers.getMapper (CardInfoMapper.class);

    @Mapping(target = "cardId", source = "cardInfo.cardId")
    @Mapping(target = "number", source = "cardInfo.number")
    @Mapping(target = "holder", source = "cardInfo.holder")
    @Mapping(target = "expirationDate", source = "cardInfo.expirationDate")
    //@Mapping(target = "user", source = "cardInfo.userId")
   // @Mapping(target = "user", ignore = true) // игнорируем объект User в DTO
    @Mapping(target = "userId", source = "cardInfo.userId.userId") // берем ID из объекта User
    CardInfoDto toDto(CardInfo cardInfo);

    /**
     * Converts {@link CardInfoDto} back to {@link CardInfo} entity.
     * <p>
     * Implements <b>reverse mapping</b> relative to {@code CardInfo -> CardInfoDto} conversion.
     * </p>
     *
     * @param cardInfoDto DTO object to convert (cannot be {@code null})
     * @return corresponding {@link CardInfo} entity
     */
    @InheritInverseConfiguration
    CardInfo toEntity (@NonNull CardInfoDto cardInfoDto);
}
