package ru.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.neoflex.deal.dto.CreditResponseDto;
import ru.neoflex.deal.entity.Credit;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CreditResponseMapper {

    @Mapping(source = "creditId", target = "creditId", qualifiedByName = "uuidToString")
    CreditResponseDto toDto(Credit credit);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}
