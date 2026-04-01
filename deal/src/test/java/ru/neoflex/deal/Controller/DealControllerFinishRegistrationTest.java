package ru.neoflex.deal.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.neoflex.deal.controller.DealController;
import ru.neoflex.deal.dto.EmploymentDto;
import ru.neoflex.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.deal.enums.*;
import ru.neoflex.deal.exception.GlobalExceptionHandler;
import ru.neoflex.deal.service.DealService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DealControllerFinishRegistrationTest {
    private MockMvc mockMvc;
    @Mock
    private DealService dealService;

    @InjectMocks
    private DealController dealController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(dealController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

    }

    @Test
    void finishRegistration_success() throws Exception {
        String statementId = UUID.randomUUID().toString().replace("-", "");
        FinishRegistrationRequestDto request = createValidRequest();

        doNothing().when(dealService)
                .finishRegistration(any(), eq(statementId));

        mockMvc.perform(post("/deal/calculate/" + statementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(dealService).finishRegistration(any(), eq(statementId));
    }

    @Test
    void finishRegistration_invalidJson_shouldReturn400() throws Exception {
        String invalidJson = "{ invalid json }";
        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(dealService, never()).finishRegistration(any(), any());
    }

    @Test
    void finishRegistration_serviceThrowsBadRequest() throws Exception {
        String id = UUID.randomUUID().toString().replace("-", "");

        doThrow(new IllegalArgumentException())
                .when(dealService).finishRegistration(any(), eq(id));

        mockMvc.perform(post("/deal/calculate/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isBadRequest());
    }


    @Test
    void finishRegistration_serviceThrowsServerError() throws Exception {
        String id = UUID.randomUUID().toString().replace("-", "");

        doThrow(new RuntimeException())
                .when(dealService).finishRegistration(any(), eq(id));

        mockMvc.perform(post("/deal/calculate/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isInternalServerError());
    }

    private FinishRegistrationRequestDto createValidRequest() {
        FinishRegistrationRequestDto dto = new FinishRegistrationRequestDto();
        dto.setGender(Gender.MALE);
        dto.setMaritalStatus(MaritalStatus.SINGLE);
        dto.setDependentAmount(1);
        dto.setPassportIssueDate(LocalDate.of(2020, 1, 1));
        dto.setPassportIssueBranch("MVD");
        dto.setAccountNumber("12345678901234567890");

        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employment.setEmployerINN("1234567890");
        employment.setSalary(new BigDecimal("50000"));
        employment.setPosition(EmploymentPosition.WORKER);
        employment.setWorkExperienceTotal(120);
        employment.setWorkExperienceCurrent(60);
        dto.setEmployment(employment);

        return dto;
    }


}
