import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class LeastRecentlyUsed<K> implements EvictionStrategy<K>{
    private final LinkedHashMap<K, Boolean> keyUsageMap = new LinkedHashMap<>(100, 0.75f, true);
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void recordKeyUsage(K key) {
        try {
            lock.lock();
            keyUsageMap.put(key, true);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public K getEvictionKey() {
        try {
            lock.lock();
            Map.Entry<K,Boolean> firstKey = keyUsageMap.firstEntry();
            return firstKey == null ? null : firstKey.getKey();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void removeKey(K key) {
        lock.lock();
        try { keyUsageMap.remove(key); }
        finally { lock.unlock(); }
    }
}
