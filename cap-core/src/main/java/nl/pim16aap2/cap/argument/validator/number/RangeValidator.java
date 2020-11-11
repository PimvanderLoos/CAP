package nl.pim16aap2.cap.argument.validator.number;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.validator.IArgumentValidator;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
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
    private final @NonNull BiFunction<@NonNull T, @NonNull T, @NonNull Boolean> lessThan;

    /**
     * The function to use to check if a number is less than another value.
     * <p>
     * This function is used like so {@code (for BiFunction<T, U, R>): return T > U;}
     */
    private final @NonNull BiFunction<@NonNull T, @NonNull T, @NonNull Boolean> moreThan;
    private final @NonNull T min;
    private final @NonNull T max;

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

    /**
     * Checks if the provided value is less than {@link #max}.
     *
     * @param input The value to compare to {@link #max}.
     * @return True if the input value is a non-null numerical value less than {@link #max}.
     */
    protected boolean lessThanMax(final @Nullable T input)
    {
        return input != null && lessThan.apply(input, max);
    }


    /**
     * Checks if the provided value is more than {@link #min}.
     *
     * @param input The value to compare to {@link #min}.
     * @return True if the input value is a non-null numerical value more than {@link #min}.
     */
    protected boolean moreThanMin(final @Nullable T input)
    {
        return input != null && moreThan.apply(input, min);
    }

    /**
     * Checks if both {@link #moreThanMin(Number)} and {@link #lessThanMax(Number)} are true.
     *
     * @param input The value to check.
     * @return True if the value is both less than {@link #max} and more than {@link #min}.
     */
    protected boolean inRange(final @Nullable T input)
    {
        return lessThanMax(input) && moreThanMin(input);
    }

    @Override
    public void validate(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                         final @NonNull Argument<T> argument, final @Nullable T input)
        throws ValidationFailureException
    {
        if (input == null || !inRange(input))
            throw new ValidationFailureException(argument, input == null ? "NULL" : input.toString(), cap.isDebug());
    }
}
