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
     * Gets a maximum validator for double values.
     *
     * @param maximum The lower limit (not inclusive!)
     * @return A new {@link MaximumValidator} for double values.
     */
    public static @NonNull MaximumValidator<Double> doubleMaximumValidator(final double maximum)
    {
        return new MaximumValidator<>(RangeValidator.doubleRangeValidator(maximum, maximum));
    }

    @Override
    public void validate(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                         final @NonNull Argument<T> argument, final @Nullable T input)
        throws ValidationFailureException
    {
        if (input == null || !rangeValidator.lessThanMax(input))
            throw new ValidationFailureException(argument, input == null ? "NULL" : input.toString(), cap.isDebug());
    }
}
