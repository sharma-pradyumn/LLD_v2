import java.io.IOException;
public class LogTester {

    public LogTester() {}

    public static void testLog() {
        FileAppender fileAppender;
        try {
            fileAppender = new FileAppender("test.log", new InMemoryFormatter());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LogOrchestrator logOrchestrator = LogOrchestrator.getInstance();
        Logger logger = logOrchestrator.getLogger(LogTester.class.getName());

        logger.addLogAppender(fileAppender);

        logger.WARN("Warning test message");
        logger.INFO("Info test message");

        fileAppender.interruptFileLogging();
    }
}
