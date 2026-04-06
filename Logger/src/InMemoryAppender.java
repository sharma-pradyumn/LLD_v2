import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryAppender implements LogAppender {
    private final Formatter inMemoryFormatter; // can use static factory to share instances
    private final List<String> inMemoryLog = new CopyOnWriteArrayList<>();

    public InMemoryAppender(Formatter inMemoryFormatter) {
        this.inMemoryFormatter = inMemoryFormatter;
    }

    @Override
    public void appendLog(LogLine logLine) {
        inMemoryLog.add(inMemoryFormatter.formatLog(logLine));
    }

    public void printLog() {
        for(String s: inMemoryLog){
            System.out.println(s);
        }
    }
}
