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
        TraceContext traceContext = TraceManager.snapShot();
        Runnable wrapRunnable = new RunnableWrapper(runnable, traceContext);
        logger.debug("runnable wrap done");
        return wrapRunnable;
    }

    @Override
    public void run() {
        TraceManager.begin(context);
        try {
            runnable.run();
        } finally {
            TraceManager.end();
        }
    }
}
