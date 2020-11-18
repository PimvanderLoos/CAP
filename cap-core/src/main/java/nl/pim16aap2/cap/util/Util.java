/*
 *  MIT License
 *
 * Copyright (c) 2020 Pim van der Loos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nl.pim16aap2.cap.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
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
    @Contract("_, !null -> !null")
    public <T> T valOrDefault(final @Nullable T value, final T fallback)
    {
        return value == null ? fallback : value;
    }

    /**
     * Gets a {@link NonNull} value from a {@link Nullable} one, with a provided fallback in case the value is null.
     *
     * @param fallback The fallback to return in case the value is null.
     * @param values   The values that may or may not be null. The first non-null will be returned.
     * @param <T>      The type of the value.
     * @return The value if it is not null, otherwise the fallback.
     */
    @Contract("!null, _ -> !null")
    public <T> T firstNonNull(final T fallback, final @Nullable T... values)
    {
        if (values == null)
            return fallback;
        for (T value : values)
            if (value != null)
                return value;
        return fallback;
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

    /**
     * Attempts to parse a double from a String.
     *
     * @param str The String to parse.
     * @return An {@link OptionalDouble} containing the double if the String was a double, otherwise {@link
     * OptionalDouble#empty()}.
     */
    public @NonNull OptionalDouble parseDouble(final @Nullable String str)
    {
        if (str == null)
            return OptionalDouble.empty();
        try
        {
            return OptionalDouble.of(Double.parseDouble(str));
        }
        catch (NumberFormatException e)
        {
            return OptionalDouble.empty();
        }
    }

    /**
     * Attempts to parse a long from a String.
     *
     * @param str The String to parse.
     * @return An {@link OptionalLong} containing the long if the String was a long, otherwise {@link
     * OptionalLong#empty()}.
     */
    public @NonNull OptionalLong parseLong(final @Nullable String str)
    {
        if (str == null)
            return OptionalLong.empty();
        try
        {
            return OptionalLong.of(Long.parseLong(str));
        }
        catch (NumberFormatException e)
        {
            return OptionalLong.empty();
        }
    }

    /**
     * Attempts to parse a UUID from a String.
     *
     * @param str The string to parse.
     * @return The UUID if the String represents one.
     */
    public @NonNull Optional<UUID> parseUUID(final @Nullable String str)
    {
        if (str == null)
            return Optional.empty();
        try
        {
            return Optional.of(UUID.fromString(str));
        }
        catch (IllegalArgumentException e)
        {
            return Optional.empty();
        }
    }

    /**
     * Checks if a value lies between two other values.
     *
     * @param test  The value to test.
     * @param start The lower bound value (exclusive).
     * @param end   The upper bound value (exclusive).
     * @return True if the test value is smaller than the lower bound and higher than the upper bound values.
     */
    public boolean between(final int test, final int start, final int end)
    {
        return test > start && test < end;
    }

    /**
     * Converts a list to a single String.
     *
     * @param lst The list to convert to a String.
     * @return The list represented by a String.
     */
    public static <T> @NonNull String listToString(final @NonNull Collection<T> lst)
    {
        return listToString(lst, Object::toString);
    }

    /**
     * Converts a list to a single String.
     *
     * @param lst    The list to convert to a String.
     * @param mapper the Function to use to convert each to a string.
     * @return The list represented by a String.
     */
    public static <T> @NonNull String listToString(final @NonNull Collection<T> lst,
                                                   final @NonNull Function<T, String> mapper)
    {
        StringBuilder sb = new StringBuilder();
        for (T arg : lst)
        {
            sb.append("\"").append(mapper.apply(arg)).append("\", ");
        }
        String res = sb.toString();
        if (res.length() > 2)
            res = res.substring(0, res.length() - 2);
        return "[" + res + "]";
    }
}
