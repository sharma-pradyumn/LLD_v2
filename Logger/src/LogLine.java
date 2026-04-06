import java.time.LocalDateTime;

public record LogLine(LocalDateTime timeStamp, LogLevel logLevel, String logText) {
}
