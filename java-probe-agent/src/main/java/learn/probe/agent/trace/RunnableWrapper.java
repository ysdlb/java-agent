package learn.probe.agent.trace;

public class RunnableWrapper implements Runnable {
    private final Runnable runnable;
    private final TraceContext context;

    public RunnableWrapper(Runnable runnable, TraceContext context) {
        this.runnable = runnable;
        this.context = context;
    }

    public static Runnable wrap(Runnable runnable) {
        TraceContext traceContext = TraceManager.snapShot();
        Runnable wrapRunnable = new RunnableWrapper(runnable, traceContext);
        System.out.println("runnable wrap done");
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
