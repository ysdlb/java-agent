package learn.probe.agent.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunnableWrapper implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RunnableWrapper.class);

    private final Runnable runnable;
    private final TraceContext context;

    public RunnableWrapper(Runnable runnable, TraceContext context) {
        this.runnable = runnable;
        this.context = context;
    }

    public static Runnable wrap(Runnable runnable) {
        System.out.format("%s%s%s\n",
                "\033[38;5;9m", "runnable wrap start", "\033[0m");
        TraceContext traceContext = TraceManager.snapShot();
        Runnable wrapRunnable = new RunnableWrapper(runnable, traceContext);
        // logger.debug("runnable wrap done");
        System.out.format("%s%s%s\n",
                "\033[38;5;9m", "runnable wrap done", "\033[0m");
        return wrapRunnable;
    }

    @Override
    public void run() {
        TraceManager.begin(context);
        System.out.format("%s%s%s\n",
                "\033[38;5;226m", "runnable run: " + Thread.currentThread().getName(), "\033[0m");
        try {
            runnable.run();
        } finally {
            TraceManager.end();
        }
    }
}
