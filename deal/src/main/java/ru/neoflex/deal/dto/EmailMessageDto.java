package ru.neoflex.deal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.neoflex.deal.enums.ThemeEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessageDto {
    private String address;
    private ThemeEnum theme;
    private String statementId;
    private String text;
}
