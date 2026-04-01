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
import ru.neoflex.deal.enums.EmploymentPosition;
import ru.neoflex.deal.enums.EmploymentStatus;
import ru.neoflex.deal.enums.Gender;
import ru.neoflex.deal.enums.MaritalStatus;
import ru.neoflex.deal.exception.GlobalExceptionHandler;
import ru.neoflex.deal.service.DealService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DealControllerFinishRegistrationValidTest {
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
    void shouldFail_whenGenderIsNull() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setGender(null);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenMaritalStatusIsNull() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setMaritalStatus(null);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenDependentAmountIsNull() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setDependentAmount(null);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenDependentAmountNegative() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setDependentAmount(-1);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenPassportIssueDateIsNull() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setPassportIssueDate(null);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenPassportIssueDateInFuture() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setPassportIssueDate(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenPassportIssueBranchIsNull() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setPassportIssueBranch(null);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenPassportIssueBranchBlank() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setPassportIssueBranch("");

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenAccountNumberIsNull() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setAccountNumber(null);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldFail_whenEmploymentDtoIsNull() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.setEmployment(null);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenEmploymentStatusIsNull() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.getEmployment().setEmploymentStatus(null);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenEmployerINNInvalid() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.getEmployment().setEmployerINN("123");

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenSalaryNegative() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.getEmployment().setSalary(BigDecimal.valueOf(-100));

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenPositionIsNull() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.getEmployment().setPosition(null);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenWorkExperienceTotalNegative() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.getEmployment().setWorkExperienceTotal(-1);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenCurrentExperienceGreaterThanTotal() throws Exception {
        FinishRegistrationRequestDto dto = createValidRequest();
        dto.getEmployment().setWorkExperienceTotal(10);
        dto.getEmployment().setWorkExperienceCurrent(20);

        mockMvc.perform(post("/deal/calculate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
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
