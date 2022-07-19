package learn.probe.agent;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolTest {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    @Test
    void threadPoolExecute() {
        int size = 2;
        ExecutorService executorPools = Executors.newFixedThreadPool(size);
        ExecutorService childExecutorPools = Executors.newFixedThreadPool(size);
        ExecutorService grandSonExecutorPools = Executors.newFixedThreadPool(size);
        for (int i = 0; i < size*2; i++) {
            String taskName = "taskName" + "-" + i;
            executorPools.submit(() -> {
                logger.info("{} submit", taskName);
                childExecutorPools.submit(() -> {
                    logger.info("{} child submit", taskName);
                    grandSonExecutorPools.submit(() -> {
                        logger.info("{} grandSon submit", taskName);
                    });
                });
            });
        }
    }

    @Test
    void completableFutureTest() {
        for (int i = 0; i < 10; i++) {
            String taskName = "taskName" + "-" + i;
            CompletableFuture.supplyAsync(() -> {
                logger.info("{} submit", taskName);
                CompletableFuture.runAsync(() -> {
                    logger.info("{} child submit", taskName);
                });
                return taskName + "R";
            });
        }
    }

    @Test
    void completableFutureTestWithThreadPool() {
        ExecutorService executorPools = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            String taskName = "taskName" + "-" + i;
            CompletableFuture.supplyAsync(() -> {
                logger.info("{} submit", taskName);
                CompletableFuture.runAsync(() -> {
                    logger.info("{} child submit", taskName);
                }, executorPools);
                return taskName + "R";
            }, executorPools);
        }
    }
}
