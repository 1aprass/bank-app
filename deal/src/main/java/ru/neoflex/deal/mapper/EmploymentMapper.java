package ru.neoflex.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.neoflex.deal.dto.EmploymentDto;
import ru.neoflex.deal.entity.Employment;

@Mapper(componentModel = "spring")
public interface EmploymentMapper {
    @Mapping(source = "position", target = "employmentPosition")
    Employment toEntity( EmploymentDto employmentDto);
}
