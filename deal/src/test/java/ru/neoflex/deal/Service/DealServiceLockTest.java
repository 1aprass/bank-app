package ru.neoflex.deal.Service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.entity.Statement;
import ru.neoflex.deal.enums.ApplicationStatus;
import ru.neoflex.deal.repository.StatementRepository;
import ru.neoflex.deal.service.DealService;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DealServiceLockTest {

    @Autowired
    private DealService dealService;

    @Autowired
    private StatementRepository statementRepository;

    private UUID statementId;

    @BeforeAll
    void setup() {
        Statement st = new Statement();
        st.setApplicationStatus(ApplicationStatus.PREAPPROVAL);
        statementRepository.save(st);
        statementId = st.getStatementId();
    }

    @Test
    void shouldWaitForLock() throws Exception {
        LoanOfferDto request = new LoanOfferDto();
        request.setStatementId(statementId);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch firstStartedLatch = new CountDownLatch(1);
        CountDownLatch firstContinueLatch = new CountDownLatch(1);

        Future<Long> first = executor.submit(() -> {
            long start = System.currentTimeMillis();
            firstStartedLatch.countDown();
            dealService.selectOffer(request);
            firstContinueLatch.await();
            long end = System.currentTimeMillis();
            return end - start;
        });

        firstStartedLatch.await();

        Future<Long> second = executor.submit(() ->{
            long start = System.currentTimeMillis();
            dealService.selectOffer(request);
            long end = System.currentTimeMillis();
            return end - start;
        });

        firstContinueLatch.countDown();

        long firstTime = first.get();
        long secondTime = second.get();
        executor.shutdown();

        assertTrue(secondTime >= firstTime);
    }

}
