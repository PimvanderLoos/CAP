package nl.pim16aap2.cap.util.cache;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

class TimedValue<T> extends AbstractTimedValue<T>
{
    private final @NonNull T value;

    /**
     * Constructor of {@link AbstractTimedValue}.
     *
     * @param val The value of this {@link AbstractTimedValue}.
     */
    public TimedValue(final @NonNull T val, final long timeOut)
    {
        super(timeOut);
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
