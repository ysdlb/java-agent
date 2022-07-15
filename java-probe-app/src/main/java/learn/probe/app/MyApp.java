package learn.probe.app;

import learn.probe.agent.trace.TraceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApp {
    private static final Logger log = LoggerFactory.getLogger(MyApp.class);

    public static void main(String[] args) {
        int number;
        do {
            Scanner reader = new Scanner(System.in);
            number = reader.nextInt();
            switch (number) {
                case 1 -> myServiceTest();
                case 2 -> threadPoolTest();
                default -> {}
            }
        } while (number != 0);
    }

    private static void myServiceTest() {
        Thread thread = Thread.currentThread();
        System.out.format("%s%s:%s%s -> main start\n",
                "\033[38;5;118m", thread.getThreadGroup().getName(), thread.getName(), "\033[0m");
        System.out.println("########### enter in 1 do something ############");

        int number;
        do {
            Scanner reader = new Scanner(System.in);
            number = reader.nextInt();
            MyService myService = new MyService();
            String ret = myService.doSomething();
            System.out.println(ret);
        } while (number == 1);
    }

    private static void threadPoolTest() {
        int size = 10;
        ExecutorService executorPools = Executors.newFixedThreadPool(size);
//        try {
//            Class.forName("learn.probe.agent.trace.RunnableWrapper");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        for (int i = 0; i < size/2; i++) {
            executorPools.submit(() -> {
                log.info("in submit: {}", TraceManager.getTraceId());
                executorPools.submit(() -> {
                    log.info("in child submit: {}", TraceManager.getTraceId());
                });
            });
        }
    }

}
