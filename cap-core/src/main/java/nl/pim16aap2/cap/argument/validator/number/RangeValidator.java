package nl.pim16aap2.cap.argument.validator.number;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.validator.IArgumentValidator;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Represents a validator for ranges. This can be used to set a lower and an upper limit for numerical input arguments.
 *
 * @param <T> The type of the number.
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class RangeValidator<T extends Number> implements IArgumentValidator<T>
{
    /**
     * The function to use to check if a number is higher than another value.
     * <p>
     * This function is used like so {@code (for BiFunction<T, U, R>): return T < U;}
     */
    final @NonNull BiFunction<@NonNull T, @NonNull T, @NonNull Boolean> lessThan;

    /**
     * The function to use to check if a number is less than another value.
     * <p>
     * This function is used like so {@code (for BiFunction<T, U, R>): return T > U;}
     */
    final @NonNull BiFunction<@NonNull T, @NonNull T, @NonNull Boolean> moreThan;
    final @NonNull T min;
    final @NonNull T max;

    /**
     * Gets a range validator for integer values.
     *
     * @param lowerLimit The lower limit (not inclusive!)
     * @param upperLimit The upper limit (not inclusive!)
     * @return A new {@link RangeValidator} for integer values.
     */
    public static @NonNull RangeValidator<Integer> integerRangeValidator(final int lowerLimit, final int upperLimit)
    {
        return new RangeValidator<>((t1, t2) -> t1 < t2,
                                    (t1, t2) -> t1 > t2,
                                    lowerLimit, upperLimit);
    }

    /**
     * Gets a range validator for double values.
     *
     * @param lowerLimit The lower limit (not inclusive!)
     * @param upperLimit The upper limit (not inclusive!)
     * @return A new {@link RangeValidator} for double values.
     */
    public static @NonNull RangeValidator<Double> doubleRangeValidator(final double lowerLimit, final double upperLimit)
    {
        return new RangeValidator<>((t1, t2) -> t1 < t2,
                                    (t1, t2) -> t1 > t2,
                                    lowerLimit, upperLimit);
    }

    @Override
    public boolean validate(final @Nullable T input)
    {
        if (input == null)
            return false;
        return moreThan.apply(input, min) && lessThan.apply(input, max);
    }
}
