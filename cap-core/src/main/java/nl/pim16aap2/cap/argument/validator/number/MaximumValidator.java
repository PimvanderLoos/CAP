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

import java.text.MessageFormat;

/**
 * Represents a validator for maximum values. This can be used to set a lower limit for numerical input arguments.
 *
 * @param <T> The type of the number.
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MaximumValidator<T extends Number> implements IArgumentValidator<T>
{
    private final @NonNull RangeValidator<T> rangeValidator;

    /**
     * Gets a maximum validator for integer values.
     *
     * @param maximum The lower limit (not inclusive!)
     * @return A new {@link MaximumValidator} for integer values.
     */
    public static @NonNull MaximumValidator<Integer> integerMaximumValidator(final int maximum)
    {
        return new MaximumValidator<>(RangeValidator.integerRangeValidator(maximum, maximum));
    }

    /**
     * Gets a maximum validator for integer values that uses {@link RangeValidator.ValueRequest}s to obtain the limit.
     *
     * @param maximumRequester The function to use to retrieve the maximum value.
     * @return A new {@link RangeValidator} for integer values.
     */
    public static @NonNull MaximumValidator<Integer> integerMaximumValidator(
        final @NonNull RangeValidator.ValueRequest<Integer> maximumRequester)
    {
        return new MaximumValidator<>(RangeValidator.integerRangeValidator(maximumRequester, maximumRequester));
    }

    /**
     * Gets a maximum validator for double values.
     *
     * @param maximum The lower limit (not inclusive!)
     * @return A new {@link MaximumValidator} for double values.
     */
    public static @NonNull MaximumValidator<Double> doubleMaximumValidator(final double maximum)
    {
        return new MaximumValidator<>(RangeValidator.doubleRangeValidator(maximum, maximum));
    }

    /**
     * Gets a maximum validator for double values that uses {@link RangeValidator.ValueRequest}s to obtain the limit.
     *
     * @param maximumRequester The function to use to retrieve the maximum value.
     * @return A new {@link RangeValidator} for double values.
     */
    public static @NonNull MaximumValidator<Double> doubleMaximumValidator(
        final @NonNull RangeValidator.ValueRequest<Double> maximumRequester)
    {
        return new MaximumValidator<>(RangeValidator.doubleRangeValidator(maximumRequester, maximumRequester));
    }

    @Override
    public void validate(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                         final @NonNull Argument<?> argument, final @Nullable T input)
        throws ValidationFailureException
    {
        final @NonNull T max = rangeValidator.getMax(cap, commandSender, argument);
        if (input == null || !rangeValidator.lessThanMax(cap, commandSender, argument, max, input))
        {
            final @NonNull String localizedMessage = MessageFormat
                .format(cap.getMessage("error.validation.maximum", commandSender.getLocale()), input, max);
            throw new ValidationFailureException(argument, input == null ? "NULL" : input.toString(), localizedMessage,
                                                 cap.isDebug());
        }
    }
}
