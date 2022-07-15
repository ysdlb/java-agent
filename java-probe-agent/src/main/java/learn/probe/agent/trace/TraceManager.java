package learn.probe.agent.trace;


import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 与 MDC 交互的 {@link TraceContext TraceContext} 管理器
 */
public class TraceManager {
    private final static Logger log = Logger.getLogger(TraceManager.class.getName());

    public static final String TRACE_KEY = "x-transaction-id";
    private static final ThreadLocal<TraceContext> CONTEXT = new ThreadLocal<>();

    public static void begin(TraceContext traceContext) {
        // todo traceContext 为 null 直接报错
        if (traceContext == null) {
            System.out.println("no traceContext found");
            return;
        }
//        if (traceContext.isFromCurrent()) {
//            log.warn("traceContext has used");
//            return;
//        }

        CONTEXT.set(traceContext);
        String traceId = traceContext.getTraceId();
        String subTraceAppend = traceContext.getSubTraceAppend();
        if (subTraceAppend != null && !subTraceAppend.isEmpty()) {
            traceId = traceId + "-" + subTraceAppend;
        }
        System.out.println("traceId: " + traceId);
    }

    public static void end() {
        CONTEXT.remove();
    }

    public static TraceContext createTraceContext(String traceId) {
        return new TraceContext(traceId, null);
    }

    public static TraceContext createTraceContext() {
        String traceId = UUID.randomUUID().toString();
        return createTraceContext(traceId);
    }

    public static void tranceIdAppend(String append) {
        if (append == null || append.isEmpty()) return;

        TraceContext context = get();
        if (context != null) {
            context.setSubTraceAppend(append);
        }
    }

    private static TraceContext get() {
        return CONTEXT.get();
    }

    /**
     * 抓取一个 traceContext 快照
     * @return traceContext 快照
     */
    public static TraceContext snapShot() {
        TraceContext traceContext = get();
        if (traceContext == null) {
            System.out.println("traceContext is null, capture a snapShot failed");
            traceContext = createTraceContext();
        }

        String traceId = traceContext.getTraceId();
        String subTraceAppend = traceContext.getSubTraceAppend();
        if (subTraceAppend != null && !subTraceAppend.isEmpty()) {
            traceId = traceId + "-" + subTraceAppend;
        }

        return createTraceContext(traceId);
    }

    public static String getTraceId() {
        return Optional.ofNullable(get())
                .map(TraceContext::getTraceId)
                .orElse(null);
    }
}
