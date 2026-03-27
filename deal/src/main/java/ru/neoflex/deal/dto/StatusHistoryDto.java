package ru.neoflex.deal.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.neoflex.deal.enums.ApplicationStatus;
import ru.neoflex.deal.enums.ChangeType;
import java.time.LocalDateTime;

@Data
@Setter
@Getter
public class StatusHistoryDto {
    private ApplicationStatus status;
    private LocalDateTime time;
    private ChangeType changeType;
}
