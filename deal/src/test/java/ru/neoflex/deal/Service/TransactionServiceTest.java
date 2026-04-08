package ru.neoflex.deal.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.neoflex.deal.entity.Statement;
import ru.neoflex.deal.repository.StatementRepository;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Service
public class TransactionServiceTest {

    @Autowired
    private StatementRepository statementRepository;

    @Transactional
    public void lockAndWait(UUID id, CountDownLatch start, CountDownLatch finish) {
        log.info("FIRST THREAD: trying to lock");

        Statement statement = statementRepository.findByIdWithLock(id)
                .orElseThrow();

        log.info("FIRST THREAD: lock acquired");

        start.countDown();

        try {
            finish.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("FIRST THREAD: releasing lock");
    }
}