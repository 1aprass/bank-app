package ru.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.neoflex.deal.dto.CreditDto;
import ru.neoflex.deal.entity.Credit;

@Mapper(componentModel = "spring")
public interface CreditMapper {
    @Mapping(source = "isInsuranceEnabled", target = "insuranceEnabled")
    @Mapping(source = "isSalaryClient", target = "salaryClient")
    Credit toEntity(CreditDto entity);
}
