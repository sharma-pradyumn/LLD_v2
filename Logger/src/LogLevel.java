public enum LogLevel {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3);

    // higher is more imp
    private final int priority;

    LogLevel(int p) {
        this.priority = p;
    }

    public int getPriority() {
        return this.priority;
    }
}
