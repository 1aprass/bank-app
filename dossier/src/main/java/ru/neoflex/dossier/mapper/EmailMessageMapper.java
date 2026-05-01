package ru.neoflex.dossier.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.neoflex.dossier.dto.EmailMessageDto;
import ru.neoflex.dossier.enums.ThemeEnum;

import java.util.Map;

@Slf4j
@Component
public class EmailMessageMapper {

    public EmailMessageDto toDto(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        ThemeEnum theme = getTheme(map, "theme");
        if(theme == null){
            log.error("EmailMessageMapper. Invalid or missing theme in message: {}", map);
            return null;
        }

        EmailMessageDto dto = new EmailMessageDto();
        dto.setAddress(getString(map, "address"));
        dto.setText(getString(map, "text"));
        dto.setStatementId(getString(map, "statementId"));
        dto.setTheme(theme);

        return dto;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private ThemeEnum getTheme(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        try {
            return ThemeEnum.valueOf(value.toString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
