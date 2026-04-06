import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileAppender implements LogAppender {
    private final int MAX_CAPACITY = 100_000;
    private final int WRITER_SLEEP_INTERVAL_MILLIS = 50;
    private final BlockingQueue<LogLine> logDeque = new ArrayBlockingQueue<LogLine>(MAX_CAPACITY);
    private volatile Formatter fileFormatter;
    private final String logFileName;
    private final BufferedWriter bufferedWriter;
    private final Thread writerThread; // Should we use execService for this ? Seems like overKill

    public FileAppender(String logFileName, Formatter fileFormatter) throws IOException {
        this.logFileName = logFileName;
        this.fileFormatter = fileFormatter;
        bufferedWriter = new BufferedWriter(new FileWriter(logFileName));
        writerThread = new Thread(this::consumeLog);
        writerThread.start();
    }

    // Ideally we should be purging the lower priortiy logs here,
    // For now, dropping the new logs
    @Override
    public void appendLog(LogLine logLine) {
        logDeque.offer(logLine);
    }

    public void setFileFormatter(Formatter fileFormatter) {
        this.fileFormatter = fileFormatter;
    }

    private void consumeLog() {
        try {
            try {
            while(true) {
                LogLine logLine = logDeque.take();
                bufferedWriter.append(fileFormatter.formatLog(logLine)).append("\n");
            }
            } catch (InterruptedException e) {
                System.out.println("FileWriter thread interrupted");
                // flush
                LogLine logLine;
                while ((logLine = logDeque.poll()) != null) {
                    bufferedWriter.append(fileFormatter.formatLog(logLine));
                }
                bufferedWriter.flush();
                Thread.currentThread().interrupt();
            }
            finally {
                bufferedWriter.close();
            }
        }
        catch (IOException e) {
            System.out.println("FileWriter faced IOException "+e.getMessage());
        }

    }


    public void interruptFileLogging() {
        writerThread.interrupt();
    }
}
