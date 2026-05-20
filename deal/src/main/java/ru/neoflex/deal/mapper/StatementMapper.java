package ru.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.neoflex.deal.dto.StatementDto;
import ru.neoflex.deal.entity.Statement;

@Mapper(componentModel = "spring", uses = {ClientMapper.class, CreditResponseMapper.class})
public interface StatementMapper {
    @Mapping(source = "statementId", target = "id")
    StatementDto toDto(Statement statement);
}
