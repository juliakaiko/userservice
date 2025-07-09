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
     * Конвертирует {@link CardInfoDto} обратно в сущность {@link CardInfo}.
     * <p>
     * Реализует <b>обратное преобразование</b> относительно маппинга {@code CardInfo -> CardInfoDto}.
     * </p>
     *
     * @param cardInfoDto DTO-объект для преобразования (не может быть {@code null})
     * @return соответствующая сущность {@link CardInfo}
     */
    @InheritInverseConfiguration
    CardInfo toEntity (@NonNull CardInfoDto cardInfoDto);
}
