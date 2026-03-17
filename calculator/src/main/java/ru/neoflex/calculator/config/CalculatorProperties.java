package ru.neoflex.calculator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "calculator")
public class CalculatorProperties {

    private int baseRate;
    private int salaryClientBonus;
    private int insuranceDiscount;

    private Insurance insurance;
    private Scoring scoring;

    @Data
    public static class Insurance {

        private Utils utils;
        private Threshold thresholds;
    }


    @Data
    public static class Utils {

        private int monthsInYear;
        private int percentConversion;
        private int daysInYear;

        public int getMonthlyDivisor() {
            return monthsInYear * percentConversion;
        }
    }

    @Data
    public static class Threshold {

        private BigDecimal maxS;
        private BigDecimal feeS;
        private BigDecimal maxM;
        private BigDecimal feeM;
        private BigDecimal maxL;
        private BigDecimal feeL;
        private BigDecimal maxXl;
        private BigDecimal feeXl;
    }

    @Data
    public static class Scoring {

        private Age age;
        private Experience experience;
        private BigDecimal salaryMultiplier;
        private RateAdjustments rateAdjustments;
    }

    @Data
    public static class Age {

        private int min;
        private int max;
    }

    @Data
    public static class Experience {

        private int totalMin;
        private int currentMin;
    }

    @Data
    public static class RateAdjustments {

        private BigDecimal married;
        private BigDecimal divorced;
        private BigDecimal selfEmployed;
        private BigDecimal businessOwner;
        private BigDecimal midManager;
        private BigDecimal topManager;

        private GenderAdjustment female;
        private GenderAdjustment male;

        private BigDecimal nonBinary;
    }

    @Data
    public static class GenderAdjustment {

        private int ageFrom;
        private int ageTo;
        private BigDecimal adjustment;
    }
}
