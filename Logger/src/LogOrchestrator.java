import java.util.concurrent.ConcurrentHashMap;

public class LogOrchestrator {
    private static final LogOrchestrator INSTANCE = new LogOrchestrator();
    private final ConcurrentHashMap<String, Logger> loggerMap = new ConcurrentHashMap<>();
    private volatile LogLevel rootLogLevel = LogLevel.INFO;

    // prevent external instantiation
    private LogOrchestrator() {}

    public void setRootLogLevel(LogLevel rootLogLevel) {
        this.rootLogLevel = rootLogLevel;
    }

    // better way to do this ?
    // Should we pass fallback LogLevel here ?
    public Logger getLogger(String className ) {
        return loggerMap.computeIfAbsent(className, (k)-> new Logger(rootLogLevel));
    }

    public static LogOrchestrator getInstance() {
        return INSTANCE;
    }

}
