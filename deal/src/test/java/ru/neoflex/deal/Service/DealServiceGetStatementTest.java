package ru.neoflex.deal.Service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.deal.dto.StatementDto;
import ru.neoflex.deal.entity.Statement;
import ru.neoflex.deal.mapper.StatementMapper;
import ru.neoflex.deal.repository.StatementRepository;
import ru.neoflex.deal.service.impl.DealServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DealServiceGetStatementTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private StatementMapper statementMapper;

    @InjectMocks
    private DealServiceImpl dealService;


    @Test
    void getStatementById_shouldReturnDto() {
        UUID statementId = UUID.randomUUID();
        Statement statement = new Statement();
        statement.setStatementId(statementId);

        StatementDto dto = new StatementDto();
        dto.setId(statementId.toString());
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));
        when(statementMapper.toDto(statement)).thenReturn(dto);
        StatementDto result = dealService.getStatementById(statementId.toString());

        assertNotNull(result);
        assertEquals(statementId.toString(), result.getId());
    }

    @Test
    void getStatementById_shouldThrow_ifStatementNotFound() {
        UUID statementId = UUID.randomUUID();
        when(statementRepository.findById(statementId)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> dealService.getStatementById(statementId.toString()));

        assertTrue(ex.getMessage().contains("Statement not found"));
    }

    @Test
    void getStatementById_shouldThrow_ifInvalidUUID() {
        String invalidId = "not-a-uuid";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> dealService.getStatementById(invalidId));

        assertTrue(ex.getMessage().contains("Invalid statementId format"));
    }


    @Test
    void getAllStatements_shouldReturnAllDtos() {
        Statement s1 = new Statement();
        Statement s2 = new Statement();
        StatementDto dto1 = new StatementDto();
        StatementDto dto2 = new StatementDto();
        when(statementRepository.findAll()).thenReturn(List.of(s1, s2));
        when(statementMapper.toDto(s1)).thenReturn(dto1);
        when(statementMapper.toDto(s2)).thenReturn(dto2);

        List<StatementDto> result = dealService.getAllStatements();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllStatements_shouldReturnEmptyList_ifNoStatements() {
        when(statementRepository.findAll()).thenReturn(List.of());
        List<StatementDto> result = dealService.getAllStatements();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}