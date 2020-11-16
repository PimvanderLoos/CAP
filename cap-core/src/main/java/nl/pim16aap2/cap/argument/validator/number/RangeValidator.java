package nl.pim16aap2.cap.argument.validator.number;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.validator.IArgumentValidator;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.util.Functional.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.function.BiFunction;

/**
 * Represents a validator for ranges. This can be used to set a lower and an upper limit for numerical input arguments.
 *
 * @param <T> The type of the number.
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    /**
     * The lower limit.
     */
    private final T min;

    /**
     * The upper limit.
     */
    private final T max;

    /**
     * The function to get the lower limit. This will be ignored if {@link #min} is not null.
     */
    private final ValueRequest<T> minRequest;

    /**
     * The function to get the upper limit. This will be ignored if {@link #max} is not null.
     */
    private final ValueRequest<T> maxRequest;

    /**
     * Gets a range validator for integer values.
     *
     * @param lowerLimit The lower limit (inclusive!)
     * @param upperLimit The upper limit (inclusive!)
     * @return A new {@link RangeValidator} for integer values.
     */
    public static @NonNull RangeValidator<Integer> integerRangeValidator(final int lowerLimit, final int upperLimit)
    {
        if (lowerLimit > upperLimit)
            throw new IllegalArgumentException(String.format("The lower limit of %d exceeds the upper limit of %d!",
                                                             lowerLimit, upperLimit));
        return new RangeValidator<>((t1, t2) -> t1 <= t2,
                                    (t1, t2) -> t1 >= t2,
                                    lowerLimit, upperLimit,
                                    null, null);
    }

    /**
     * Gets a range validator for integer values that uses {@link ValueRequest}s to obtain the limits.
     *
     * @param lowerLimitRequester The function to use to retrieve the lower limit.
     * @param upperLimitRequester The function to use to retrieve the upper limit.
     * @return A new {@link RangeValidator} for integer values.
     */
    public static @NonNull RangeValidator<Integer> integerRangeValidator(
        final @NonNull ValueRequest<Integer> lowerLimitRequester,
        final @NonNull ValueRequest<Integer> upperLimitRequester)
    {
        return new RangeValidator<>((t1, t2) -> t1 <= t2,
                                    (t1, t2) -> t1 >= t2,
                                    null, null,
                                    lowerLimitRequester, upperLimitRequester);
    }

    /**
     * Gets a range validator for double values.
     *
     * @param lowerLimit The lower limit (inclusive!)
     * @param upperLimit The upper limit (inclusive!)
     * @return A new {@link RangeValidator} for double values.
     */
    public static @NonNull RangeValidator<Double> doubleRangeValidator(final double lowerLimit, final double upperLimit)
    {
        if (lowerLimit > upperLimit)
            throw new IllegalArgumentException(String.format("The lower limit of %f exceeds the upper limit of %f!",
                                                             lowerLimit, upperLimit));
        return new RangeValidator<>((t1, t2) -> t1 <= t2,
                                    (t1, t2) -> t1 >= t2,
                                    lowerLimit, upperLimit,
                                    null, null);
    }

    /**
     * Gets a range validator for double values that uses {@link ValueRequest}s to obtain the limits.
     *
     * @param lowerLimitRequester The function to use to retrieve the lower limit.
     * @param upperLimitRequester The function to use to retrieve the upper limit.
     * @return A new {@link RangeValidator} for integer values.
     */
    public static @NonNull RangeValidator<Double> doubleRangeValidator(
        final @NonNull ValueRequest<Double> lowerLimitRequester,
        final @NonNull ValueRequest<Double> upperLimitRequester)
    {
        return new RangeValidator<>((t1, t2) -> t1 <= t2,
                                    (t1, t2) -> t1 >= t2,
                                    null, null,
                                    lowerLimitRequester, upperLimitRequester);
    }

    /**
     * Checks if the provided value is less than {@link #max}.
     * <p>
     * If {@link #max} is not provided, {@link #maxRequest} will be used to obtain the value.
     *
     * @param input The value to compare to {@link #max}.
     * @return True if the input value is a non-null numerical value less than {@link #max}.
     */
    protected boolean lessThanMax(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                  final @NonNull Argument<?> argument, final @NonNull T max, final @Nullable T input)
    {
        return input != null && lessThan.apply(input, max);
    }

    /**
     * Checks if the provided value is more than {@link #min}.
     * <p>
     * If {@link #min} is not provided, {@link #minRequest} will be used to obtain the value.
     *
     * @param input The value to compare to {@link #min}.
     * @return True if the input value is a non-null numerical value more than {@link #min}.
     */
    protected boolean moreThanMin(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                  final @NonNull Argument<?> argument, final @NonNull T min, final @Nullable T input)
    {

        return input != null && moreThan.apply(input, min);
    }

    /**
     * Gets the minimum value. This is either {@link #min} (if it was supplied) and otherwise the result of {@link
     * #minRequest}.
     *
     * @param cap           The {@link CAP} instance for which to validate the input.
     * @param commandSender The {@link ICommandSender} for which to validate the input.
     * @param argument      The {@link Argument} to validate the input for.
     * @return The lower bound value.
     */
    protected @NonNull T getMin(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                final @NonNull Argument<?> argument)
    {
        return min == null ? minRequest.apply(cap, commandSender, argument) : min;
    }

    /**
     * Gets the maximum value. This is either {@link #max} (if it was supplied) and otherwise the result of {@link
     * #maxRequest}.
     *
     * @param cap           The {@link CAP} instance for which to validate the input.
     * @param commandSender The {@link ICommandSender} for which to validate the input.
     * @param argument      The {@link Argument} to validate the input for.
     * @return The upper bound value.
     */
    protected @NonNull T getMax(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                final @NonNull Argument<?> argument)
    {
        return max == null ? maxRequest.apply(cap, commandSender, argument) : max;
    }

    /**
     * Checks if both {@link #moreThanMin(CAP, ICommandSender, Argument, Number, Number)} and {@link #lessThanMax(CAP,
     * ICommandSender, Argument, Number, Number)} are true.
     *
     * @param input The value to check.
     * @return True if the value is both less than {@link #max} and more than {@link #min}.
     */
    protected boolean inRange(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                              final @NonNull Argument<?> argument, final @NonNull T min, final @NonNull T max,
                              final @Nullable T input)
    {
        return lessThanMax(cap, commandSender, argument, max, input) &&
            moreThanMin(cap, commandSender, argument, min, input);
    }

    @Override
    public void validate(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                         final @NonNull Argument<?> argument, final @Nullable T input)
        throws ValidationFailureException
    {
        final @NonNull T min = getMin(cap, commandSender, argument);
        final @NonNull T max = getMax(cap, commandSender, argument);

        if (input == null || !inRange(cap, commandSender, argument, min, max, input))
        {
            final @NonNull String localizedMessage = MessageFormat
                .format(cap.getLocalizer().getMessage("error.validation.range", commandSender), input, min, max);
            throw new ValidationFailureException(argument, input == null ? "NULL" : input.toString(), localizedMessage,
                                                 cap.isDebug());
        }
    }

    @FunctionalInterface
    public interface ValueRequest<T>
        extends TriFunction<@NonNull CAP, @NonNull ICommandSender, @NonNull Argument<?>, @NonNull T>
    {
    }
}
