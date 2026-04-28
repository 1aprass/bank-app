package ru.neoflex.dossier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.neoflex.dossier.enums.ThemeEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessageDto {
    private String address;
    private String statementId;
    private String text;
    private ThemeEnum theme;
}