package nl.pim16aap2.commandparser.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

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

    /**
     * Gets a {@link NonNull} value from a {@link Nullable} one, with a provided fallback in case the value is null.
     *
     * @param value    The value that may or may not be null.
     * @param fallback A {@link Supplier} to supply a fallback to return in case the value is null.
     * @param <T>      The type of the value.
     * @return The value if it is not null, otherwise the fallback.
     */
    public @NonNull <T> T valOrDefault(final @Nullable T value, final @NonNull Supplier<T> fallback)
    {
        return value == null ? fallback.get() : value;
    }

    /**
     * Searches through an {@link Iterable} object using a provided search function.
     *
     * @param iterable  The {@link Iterable} object to search through.
     * @param searchFun The search function to use. This function should return {@link Boolean#TRUE} for the value that
     *                  is being searched for.
     * @param <T>       The type of objects stored in the {@link Iterable}.
     * @return The value in the {@link Iterable} object for which the search function returns true, otherwise {@link
     * Optional#empty()}.
     */
    public @NonNull <T> Optional<T> searchIterable(final @NonNull Iterable<T> iterable,
                                                   final @NonNull Function<T, Boolean> searchFun)
    {
        for (final T val : iterable)
            if (searchFun.apply(val))
                return Optional.of(val);
        return Optional.empty();
    }

    /**
     * Attempts to parse an integer from a String.
     *
     * @param str The String to parse.
     * @return An {@link OptionalInt} containing the integer if the String was an integer, otherwise {@link
     * OptionalInt#empty()}.
     */
    public @NonNull OptionalInt parseInt(final @Nullable String str)
    {
        if (str == null)
            return OptionalInt.empty();
        try
        {
            return OptionalInt.of(Integer.parseInt(str));
        }
        catch (NumberFormatException e)
        {
            return OptionalInt.empty();
        }
    }
}
