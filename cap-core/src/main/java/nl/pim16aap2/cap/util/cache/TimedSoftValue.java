package nl.pim16aap2.cap.util.cache;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.Objects;

class TimedSoftValue<T> extends AbstractTimedValue<T>
{
    private final @NonNull SoftReference<T> value;

    public TimedSoftValue(final @NonNull T val, final long timeOut)
    {
        super(timeOut);
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
