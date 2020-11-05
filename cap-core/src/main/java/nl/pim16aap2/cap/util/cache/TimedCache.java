package nl.pim16aap2.cap.util.cache;

import lombok.Builder;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Represents a timed cached map backed by a {@link ConcurrentHashMap}. Entries will expire after a configurable amount
 * of time.
 * <p>
 * Expired entries cannot be retrieved or used in any way, but they will still show up in the size arguments. If
 * configured, a separate thread may perform regular cleanup.
 *
 * @param <K> Type of the Key of the map.
 * @param <V> Type of the value of the map.
 * @author Pim
 */
public class TimedCache<K, V>
{
    /**
     * The actual datastructure all values are cached in.
     */
    private final @NonNull ConcurrentHashMap<K, AbstractTimedValue<V>> cache = new ConcurrentHashMap<>();

    /**
     * The amount of time a variable will be available measured in milliseconds for positive non-zero values.
     * <p>
     * 0 means values are kept forever,
     * <p>
     * < 0 values mean nothing ever gets added in the first place.
     */
    private final long timeOut;

    /**
     * Function that creates the specific type of {@link AbstractTimedValue} that is required according to the
     * configuration.
     */
    private final @NonNull Function<V, AbstractTimedValue<V>> timedValueCreator;

    /**
     * Whether to refresh entries whenever they are accessed.
     * <p>
     * When set to false, entries will expire after the configured amount of time after their insertion time.
     * <p>
     * When set to true, entries will expire  after the configured amount of time after they were last retrieved.
     */
    private final boolean refresh;

    /**
     * Constructor of {@link TimedCache}
     *
     * @param duration      The amount of time a cached entry remains valid.
     *                      <p>
     *                      Note that this value is used for millisecond precision. Anything smaller than that will be
     *                      ignored.
     * @param cleanup       The duration between each cleanup cycle. During cleanup, all expired entries will be removed
     *                      from the cache. When null or 0, entries are evicted from the cache whenever they are
     *                      accessed after they have expired. This value also uses millisecond precision.
     * @param softReference Whether to wrap values in {@link SoftReference}s or not. This allows the garbage collector
     *                      to clear up any values as it sees fit.
     * @param refresh       Whether to refresh entries whenever they are accessed.
     *                      <p>
     *                      When set to false, entries will expire after the configured amount of time after their
     *                      insertion time.
     *                      <p>
     *                      When set to true, entries will expire  after the configured amount of time after they were
     *                      last retrieved.
     */
    @Builder
    protected TimedCache(final @NonNull Duration duration, final @Nullable Duration cleanup,
                         final boolean softReference, final boolean refresh)
    {
        timeOut = duration.toMillis();
        timedValueCreator = softReference ? this::createTimedSoftValue : this::createTimedValue;
        setupCleanupTask(cleanup == null ? 0 : cleanup.toMillis());
        this.refresh = refresh;
    }

    /**
     * Puts a new key/value pair in the cache.
     *
     * @param key   The key of the pair to add to the cache.
     * @param value The value of the pair to add to the cache.
     * @return The value that was just added to the cache.
     */
    public @NonNull V put(final @NonNull K key, final @NonNull V value)
    {
        cache.put(key, timedValueCreator.apply(value));
        return value;
    }

    /**
     * See {@link ConcurrentHashMap#computeIfAbsent(Object, Function)}.
     */
    public @NonNull V computeIfAbsent(final @NonNull K key, final @NonNull Function<K, @NonNull V> mappingFunction)
    {
        // We can't use ConcurrentHashMap#computeIfAbsent(Object, Function) because we don't just want the value to be
        // present, we want it to be available (i.e. does not exceed time limit).
        final @Nullable AbstractTimedValue<V> current = cache.get(key);
        if (current != null)
        {
            final @Nullable V currentValue = current.getValue();
            if (currentValue != null)
            {
                if (refresh)
                    current.refresh();
                return currentValue;
            }
        }

        return put(key, mappingFunction.apply(key));
    }

    /**
     * See {@link ConcurrentHashMap#remove(Object)}.
     */
    public @NonNull Optional<V> remove(final @NonNull K key)
    {
        return getValue(cache.remove(key));
    }

    /**
     * Gets the value associated with the provided key.
     * <p>
     * If the value has expired but still exists in the map, it will be evicted and treated as if it did not exist at
     * all.
     *
     * @param key The key of the value to look up.
     * @return The value associated with the provided key if it is available.
     */
    public @NonNull Optional<V> get(final @NonNull K key)
    {
        final @Nullable AbstractTimedValue<V> entry = cache.get(key);
        if (entry == null)
            return Optional.empty();

        if (entry.timedOut())
        {
            cache.remove(key);
            return Optional.empty();
        }
        if (refresh)
            entry.refresh();
        return Optional.ofNullable(entry.getValue());
    }

    /**
     * Wraps a value in an {@link Optional}. If the provided entry is not null, it will retrieve the value wrapped
     * inside it.
     * <p>
     * See {@link AbstractTimedValue#getValue()}.
     *
     * @param entry The entry to wrap.
     * @return The value stored in the entry, if any.
     */
    private @NonNull Optional<V> getValue(final @Nullable AbstractTimedValue<V> entry)
    {
        return entry == null ? Optional.empty() : Optional.ofNullable(entry.getValue());
    }

    /**
     * Gets the total number cached entries.
     * <p>
     * Note that this also includes expired entries.
     *
     * @return The total number of cached entries.
     */
    public int getSize()
    {
        return cache.size();
    }

    /**
     * Removes all entries from the cache.
     */
    public void clear()
    {
        cache.clear();
    }

    /**
     * Creates a new {@link TimedValue}. This method should not be called directly. Instead, use to {@link
     * #timedValueCreator}.
     *
     * @param val The value to wrap in an {@link AbstractTimedValue}.
     * @return The newly created {@link TimedValue}.
     */
    private AbstractTimedValue<V> createTimedValue(final @NonNull V val)
    {
        return new TimedValue<>(val, timeOut);
    }

    /**
     * Creates a new {@link TimedSoftValue}. This method should not be called directly. Instead, use to {@link
     * #timedValueCreator}.
     *
     * @param val The value to wrap in an {@link AbstractTimedValue}.
     * @return The newly created {@link TimedSoftValue}.
     */
    private AbstractTimedValue<V> createTimedSoftValue(final @NonNull V val)
    {
        return new TimedSoftValue<>(val, timeOut);
    }

    /**
     * Removes any entries that have expired from the map.
     * <p>
     * An entry counts as expired if {@link AbstractTimedValue#getValue()} returns null.
     */
    private void cleanupCache()
    {
        for (Map.Entry<K, AbstractTimedValue<V>> entry : cache.entrySet())
        {
            if (entry.getValue().getValue() == null)
                cache.remove(entry.getKey());
        }
    }

    /**
     * Creates the cleanup task that will clean up the cache every 'period' milliseconds.
     * <p>
     * See {@link #cleanupCache()}.
     *
     * @param period The amount of time (in milliseconds) between each cleanup run. If this value is less than 1,
     *               nothing happens.
     */
    private void setupCleanupTask(final long period)
    {
        if (period < 1)
            return;

        final @NonNull Timer taskTimer = new Timer(true);
        final @NonNull TimerTask verifyTask = new TimerTask()
        {
            @Override
            public void run()
            {
                cleanupCache();
            }
        };
        taskTimer.scheduleAtFixedRate(verifyTask, period, period);
    }
}
