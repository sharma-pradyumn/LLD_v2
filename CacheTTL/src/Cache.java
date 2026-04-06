import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/// Main orchestrator class
/// exposes get, put methods
/// Implements periodic cleanup of stale keys in background

public class Cache<K,V> {
    private final ConcurrentHashMap<K,ValueRecord<V>> cacheMap = new ConcurrentHashMap<>();
    public long capacity;
    private EvictionStrategy<K> evictionStrategy;
    private final AtomicLong currentSize = new AtomicLong(0);
    private long staleEvictionPeriodSeconds;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public Cache(long capacity, EvictionStrategy<K> evictionStrategy, long staleEvictionPeriodSeconds) {
        this.capacity = capacity;
        this.evictionStrategy = evictionStrategy;
        this.staleEvictionPeriodSeconds = staleEvictionPeriodSeconds;
        scheduler.scheduleWithFixedDelay(this::evictStaleKeys, staleEvictionPeriodSeconds, staleEvictionPeriodSeconds, TimeUnit.SECONDS);
    }

    // evict null value keys and stale keys
    private synchronized void evictStaleKeys(){
        for (var entry : cacheMap.entrySet()) {
            ValueRecord<V> v = entry.getValue();
            if (v.isExpired()) {
                cacheMap.remove(entry.getKey(), v);
                evictionStrategy.removeKey(entry.getKey());
                currentSize.decrementAndGet();
            }
        }
    }

    public void setEvictionStrategy(EvictionStrategy<K> evictionStrategy) {
        this.evictionStrategy = evictionStrategy;
    }

    // Put also calls evictStaleKeys, Ok as synchronized is Reentrant
    public synchronized void put(K key, V value, Duration liveDuration){
        this.evictStaleKeys(); // optional, if we wanna prevent redundant live eviction
        boolean isNewKey = !cacheMap.containsKey(key);
        if (isNewKey) {
            if (currentSize.get() == capacity) {
                cacheMap.remove(evictionStrategy.getEvictionKey());
                currentSize.getAndDecrement();
            }
            currentSize.getAndIncrement(); // increase currentSize
        }
        evictionStrategy.recordKeyUsage(key);
        cacheMap.put(key, new ValueRecord<>(value, Instant.now().plus(liveDuration)));
    }

    public V get(K key){

       // Here we have side effect in compute, alt is below
        ValueRecord<V> valueRecord = cacheMap.compute(key, (k, oldValue)-> {
          if (oldValue == null) {
              evictionStrategy.removeKey(key);
              return null; // setting val as null auto removes from CHM
          }
          if (oldValue.isExpired()) {
              currentSize.getAndDecrement(); // reduce currentSize
              evictionStrategy.removeKey(key);
              return null;
          }
            evictionStrategy.recordKeyUsage(key); // record only valid hits
            return oldValue;
        });
        if (valueRecord == null) {
            return null;
        }
        return valueRecord.value();

        /// Small race condition here, but acceptable. Alt is to make the entire thing synchronized
        /// AtomicBoolean wasEvicted = new AtomicBoolean(false);
        ///
        /// ValueRecord<V> result = cacheMap.compute(key, (k, old) -> {
        ///     if (old == null) return null;
        ///     if (old.isExpired()) {
        ///         wasEvicted.set(true);
        ///         return null;
        ///     }
        ///     return old;
        /// });
    };

    public void shutdownBackgroundEviction(){
        scheduler.shutdown();
        try {
            if(!scheduler.awaitTermination(staleEvictionPeriodSeconds, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
             System.out.println("Eviction scheduler shutdown was interrupted");
        }
    }

}
