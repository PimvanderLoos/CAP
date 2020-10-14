package nl.pim16aap2.commandparser.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class Util
{
    /**
     * Gets a {@link NonNull} value from a {@link Nullable} one, with a provided fallback in case the value is null.
     *
     * @param value    The value that may or may not be null.
     * @param fallback The fallback to return in case the value is null.
     * @param <T>      The type of the value.
     * @return The value if it is not null, otherwise the fallback.
     */
    public @NonNull <T> T valOrDefault(final @Nullable T value, final @NonNull T fallback)
    {
        return value == null ? fallback : value;
    }
}
