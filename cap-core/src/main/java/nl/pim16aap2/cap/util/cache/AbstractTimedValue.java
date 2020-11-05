package nl.pim16aap2.cap.util.cache;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;

/**
 * Represents a value in a {@link TimedCache}. It holds the value and the time of insertion.
 *
 * @param <T> Type of the value.
 */
abstract class AbstractTimedValue<T>
{
    protected final long timeOut;
    protected long insertTime;
    protected final @NonNull Clock clock;

    protected AbstractTimedValue(final @NonNull Clock clock, final long timeOut)
    {
        this.clock = clock;
        this.timeOut = timeOut;
        refresh();
    }

    /**
     * Refreshes the insertion time of this timed value. This updates the {@link #insertTime} to the current time.
     */
    public void refresh()
    {
        insertTime = clock.millis();
    }

    /**
     * Gets the value wrapped inside this {@link AbstractTimedValue}.
     * <p>
     * If this value is not accessible (e.g. exceeds {@link #timeOut} or the value itself has become invalid), null is
     * returned.
     *
     * @return The value wrapped inside this {@link AbstractTimedValue}.
     */
    public abstract @Nullable T getValue();

    /**
     * Check if this {@link AbstractTimedValue} was inserted more than milliseconds ago. If so, it's considered "timed
     * out".
     *
     * @return True if the value has timed out.
     */
    public boolean timedOut()
    {
        if (timeOut == 0)
            return false;
        return (clock.millis() - insertTime) > timeOut;
    }
}
