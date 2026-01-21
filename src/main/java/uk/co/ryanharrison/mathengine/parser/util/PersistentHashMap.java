package uk.co.ryanharrison.mathengine.parser.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Persistent (immutable) hash map with structural sharing.
 * <p>
 * Copy-on-write operations share unchanged structure between copies.
 * <p>
 * Performance:
 * <ul>
 *     <li>get: O(d) where d is chain depth (typically small)</li>
 *     <li>assoc (put): O(1) - creates new map with shared structure</li>
 *     <li>Memory: Shares most structure with parent, only stores modified entries</li>
 * </ul>
 *
 * <h2>Use Case:</h2>
 * Closure capture - instead of copying all variables (O(n)),
 * we create a new map that shares structure with parent (O(1) space, O(1) time).
 *
 * @param <K> key type
 * @param <V> value type
 */
public class PersistentHashMap<K, V> {

    private final Map<K, V> local;
    private final PersistentHashMap<K, V> parent;

    private PersistentHashMap(Map<K, V> local, PersistentHashMap<K, V> parent) {
        this.local = local;
        this.parent = parent;
    }

    /**
     * Creates an empty persistent map.
     */
    public static <K, V> PersistentHashMap<K, V> empty() {
        return new PersistentHashMap<>(new HashMap<>(), null);
    }

    /**
     * Creates a persistent map from an existing map.
     */
    public static <K, V> PersistentHashMap<K, V> from(Map<K, V> map) {
        return new PersistentHashMap<>(new HashMap<>(map), null);
    }

    /**
     * Associates a key with a value, returning a new map.
     * Original map is unchanged.
     *
     * @param key   the key
     * @param value the value
     * @return new map with the association added
     */
    public PersistentHashMap<K, V> assoc(K key, V value) {
        var newLocal = new HashMap<K, V>();
        newLocal.put(key, value);
        return new PersistentHashMap<>(newLocal, this);
    }

    /**
     * Associates multiple key-value pairs, returning a new map.
     * Original map is unchanged.
     *
     * @param entries the entries to add
     * @return new map with all associations added
     */
    public PersistentHashMap<K, V> assocAll(Map<K, V> entries) {
        if (entries.isEmpty()) {
            return this;
        }
        return new PersistentHashMap<>(new HashMap<>(entries), this);
    }

    /**
     * Dissociates a key, returning a new map without that key.
     * Original map is unchanged.
     * <p>
     * Note: This marks the key as removed in the new map's local layer,
     * effectively shadowing any parent value.
     *
     * @param key the key to remove
     * @return new map without the key
     */
    public PersistentHashMap<K, V> dissoc(K key) {
        // We use a sentinel approach - store null to shadow parent values
        // The containsKey check in get() handles this
        var newLocal = new HashMap<K, V>();
        newLocal.put(key, null);
        return new PersistentHashMap<>(newLocal, this) {
            private final Set<K> removed = Set.of(key);

            @Override
            public V get(K k) {
                if (removed.contains(k)) {
                    return null;
                }
                return super.get(k);
            }

            @Override
            public boolean containsKey(K k) {
                if (removed.contains(k)) {
                    return false;
                }
                return super.containsKey(k);
            }
        };
    }

    /**
     * Gets value for key, searching up parent chain.
     *
     * @param key the key to look up
     * @return the value, or null if not found
     */
    public V get(K key) {
        // Search local map first
        if (local.containsKey(key)) {
            return local.get(key);
        }

        // Search parent chain
        return parent != null ? parent.get(key) : null;
    }

    /**
     * Checks if key exists anywhere in chain.
     *
     * @param key the key to check
     * @return true if the key exists
     */
    public boolean containsKey(K key) {
        return local.containsKey(key) || (parent != null && parent.containsKey(key));
    }

    /**
     * Returns the number of entries in the flattened map.
     * Note: This is O(n) as it requires traversing the chain.
     *
     * @return total number of unique keys
     */
    public int size() {
        return toMap().size();
    }

    /**
     * Checks if the map is empty.
     * Note: This is O(n) as it may require traversing the chain to account for dissoc.
     *
     * @return true if no entries exist
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Flattens entire chain into a single map.
     * Note: This is O(n) where n is total entries across all layers.
     * Only use when you need a true snapshot.
     *
     * @return flattened map containing all entries
     */
    public Map<K, V> toMap() {
        var result = new HashMap<K, V>();
        collectInto(result);
        return result;
    }

    /**
     * Returns the set of all keys in this map.
     * Note: This is O(n) as it requires traversing the chain.
     *
     * @return set of all keys
     */
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        collectKeys(keys);
        return keys;
    }

    private void collectInto(Map<K, V> target) {
        if (parent != null) {
            parent.collectInto(target);
        }
        // Local entries override parent entries
        for (Map.Entry<K, V> entry : local.entrySet()) {
            if (entry.getValue() != null) {
                target.put(entry.getKey(), entry.getValue());
            } else {
                // null value means key was dissociated
                target.remove(entry.getKey());
            }
        }
    }

    private void collectKeys(Set<K> keys) {
        if (parent != null) {
            parent.collectKeys(keys);
        }
        for (Map.Entry<K, V> entry : local.entrySet()) {
            if (entry.getValue() != null) {
                keys.add(entry.getKey());
            } else {
                keys.remove(entry.getKey());
            }
        }
    }

    @Override
    public String toString() {
        return toMap().toString();
    }
}
