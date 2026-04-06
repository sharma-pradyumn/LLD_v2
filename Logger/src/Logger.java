import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Logger {
    private volatile LogLevel currentLogLevel;
    private final List<LogAppender> logAppenders = new CopyOnWriteArrayList<>();

    public Logger(LogLevel initialLogLevel) {
        currentLogLevel = initialLogLevel;
    }

    public Logger() {
    }

    public void setCurrentLogLevel(LogLevel currentLogLevel) {
        this.currentLogLevel = currentLogLevel;
    }

    public void addLogAppender(LogAppender l) {
        // null check if FileAppenders face IOException and push null
        if (l == null) {
            throw new IllegalArgumentException("LogAppender cannot be null");
        }
        logAppenders.add(l);
    }

    public void removeLogAppender(LogAppender l) {
        logAppenders.remove(l);
    }

    // do we need sync here, or ok to do parallel iteration ? or is it required that all logging happens in order ?

    /// Ans -> For sync appenders (Console, File) — strict ordering is expected within a single thread, but across threads,
    /// arrival order = scheduling order, which is non-deterministic by nature.
    /// You can't guarantee global ordering without a single serialization point, which kills throughput.
    /// What you must guarantee is no interleaving mid-message — one complete log line, then the next.
    /// For AsyncAppender — the BlockingQueue gives you FIFO ordering from enqueue to dequeue, so messages are processed in the order they were enqueued.
    /// But enqueue order across threads is again scheduling-dependent.
    /// SDE2 expectation: no mid-message interleaving, FIFO within a single thread, best-effort across threads.
    /// Global ordering across threads requires a sequence number + reorder buffer — that's overkill here, flag it as a known limitation.

    private void addLogLine(LogLevel logLevel, String logString) {
        LogLine logLine = new LogLine(LocalDateTime.now(), logLevel, logString);
        if (currentLogLevel == null || logLevel.getPriority() < currentLogLevel.getPriority()) {
            return;
        }
        for(LogAppender logAppender: logAppenders) {
                logAppender.appendLog(logLine);
                // call here is synchronous, individual appenders manage async
        }
    }

    public void DEBUG(String logString) {
        addLogLine(LogLevel.DEBUG, logString);
    }

    public void INFO(String logString) {
        addLogLine(LogLevel.INFO, logString);
    }

    public void WARN(String logString) {
        addLogLine(LogLevel.WARN, logString);
    }

    public void ERROR(String logString) {
        addLogLine(LogLevel.ERROR, logString);
    }
}
