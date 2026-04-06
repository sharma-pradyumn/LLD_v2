import java.time.Instant;

public record ValueRecord<V>(V value, Instant timeToLive) {
    public boolean isExpired() {
        return this.timeToLive().isBefore(Instant.now());
    }
}
