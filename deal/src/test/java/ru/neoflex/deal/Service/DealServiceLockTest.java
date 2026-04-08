package ru.neoflex.deal.Service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.entity.Statement;
import ru.neoflex.deal.enums.ApplicationStatus;
import ru.neoflex.deal.repository.StatementRepository;
import ru.neoflex.deal.service.DealService;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DealServiceLockTest {

    @Autowired
    private DealService dealService;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private TransactionServiceTest transactionServiceTest;


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

        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);

        Future<?> first = executor.submit(() -> {
            transactionServiceTest.lockAndWait(statementId, firstLocked, releaseFirst);
        });

        firstLocked.await();

        long start = System.currentTimeMillis();

        Future<?> second = executor.submit(() -> {
            dealService.selectOffer(request);
        });

        Thread.sleep(5000);
        releaseFirst.countDown();
        first.get();
        second.get();
        long duration = System.currentTimeMillis() - start;
        executor.shutdown();

        assertTrue(duration >= 5000);
        assertTrue(firstLocked.await(2, TimeUnit.SECONDS));

    }

}
