package nl.pim16aap2.cap.util.cache;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;

/**
 * Represents a basic implementation of a {@link AbstractTimedValue}.
 *
 * @param <T> The type of the value to store.
 * @author Pim
 */
class TimedValue<T> extends AbstractTimedValue<T>
{
    private final @NonNull T value;

    /**
     * Constructor of {@link TimedValue}.
     *
     * @param clock   The {@link Clock} to use to determine anything related to time (insertion, age).
     * @param val     The value of this {@link TimedValue}.
     * @param timeOut The amount of time (in milliseconds) before this entry expires.
     */
    public TimedValue(final @NonNull Clock clock, final @NonNull T val, final long timeOut)
    {
        super(clock, timeOut);
        value = val;
    }

    @Override
    public @Nullable T getValue()
    {
        if (timedOut())
            return null;
        return value;
    }
}
