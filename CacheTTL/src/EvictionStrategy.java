public interface EvictionStrategy<K> {
    public void recordKeyUsage(K key);
    public K getEvictionKey();
    public void removeKey(K key);
}
