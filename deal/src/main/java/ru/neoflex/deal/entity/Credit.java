package ru.neoflex.deal.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import ru.neoflex.deal.dto.PaymentScheduleElementDto;
import ru.neoflex.deal.enums.CreditStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "credit")
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "credit_id", nullable = false)
    private UUID creditId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "term", nullable = false)
    private Integer term;

    @Column(name = "monthly_payment", nullable = false)
    private BigDecimal monthlyPayment;

    @Column(name = "rate", nullable = false)
    private BigDecimal rate;

    @Column(name = "psk", nullable = false)
    private BigDecimal psk;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<PaymentScheduleElementDto> paymentSchedule;

    @Column(name = "insurance_enabled", nullable = false)
    private Boolean insuranceEnabled;

    @Column(name = "salary_client", nullable = false)
    private Boolean salaryClient;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_status", nullable = false)
    private CreditStatus creditStatus;


}
