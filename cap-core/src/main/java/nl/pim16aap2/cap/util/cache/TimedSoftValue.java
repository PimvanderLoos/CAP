package nl.pim16aap2.cap.util.cache;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.time.Clock;
import java.util.Objects;

/**
 * Represents an {@link AbstractTimedValue} wrapped in a {@link SoftReference}.
 *
 * @param <T> The type of the value to store.
 * @author Pim
 */
class TimedSoftValue<T> extends AbstractTimedValue<T>
{
    private final @NonNull SoftReference<T> value;

    /**
     * Constructor of {@link TimedSoftValue}.
     *
     * @param clock   The {@link Clock} to use to determine anything related to time (insertion, age).
     * @param val     The value of this {@link TimedSoftValue}.
     * @param timeOut The amount of time (in milliseconds) before this entry expires.
     */
    public TimedSoftValue(final @NonNull Clock clock, final @NonNull T val, final long timeOut)
    {
        super(clock, timeOut);
        value = new SoftReference<>(val);
    }

    @Override
    public @Nullable T getValue()
    {
        if (timedOut())
            return null;
        return value.get();
    }

    @Override
    public boolean timedOut()
    {
        return super.timedOut() || value.get() == null;
    }

    /**
     * Gets the raw {@link SoftReference}-wrapped value.
     *
     * @return The raw value, warpped in a {@link SoftReference}.
     */
    public @NonNull SoftReference<T> getRawValue()
    {
        return value;
    }

    @Override
    public boolean equalsValue(final @Nullable Object o)
    {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o instanceof TimedSoftValue)
            return Objects.equals(getValue(), ((TimedSoftValue) o).getValue());
        return false;
    }
}
