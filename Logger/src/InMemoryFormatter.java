public class InMemoryFormatter implements Formatter{
    @Override
    public String formatLog(LogLine logLine) {
        return "["+logLine.logLevel()+"] - {"+logLine.timeStamp()+"}: "+logLine.logText();
    }
}
