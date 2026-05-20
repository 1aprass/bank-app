package ru.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.neoflex.deal.dto.ClientDto;
import ru.neoflex.deal.entity.Client;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    ClientDto toDto(Client client);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}
