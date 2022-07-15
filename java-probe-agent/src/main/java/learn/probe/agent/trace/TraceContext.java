package learn.probe.agent.trace;

public class TraceContext {
    private String traceId;
    private String subTraceAppend;

    public TraceContext(String traceId, String subTraceAppend) {
        this.traceId = traceId;
        this.subTraceAppend = subTraceAppend;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSubTraceAppend() {
        return subTraceAppend;
    }

    public void setSubTraceAppend(String subTraceAppend) {
        this.subTraceAppend = subTraceAppend;
    }
}
