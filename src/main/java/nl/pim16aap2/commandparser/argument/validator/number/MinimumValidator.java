package nl.pim16aap2.commandparser.argument.validator.number;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.validator.IArgumentValidator;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a validator for minimum values. This can be used to set a lower limit for numerical input arguments.
 *
 * @param <T> The type of the number.
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MinimumValidator<T extends Number> implements IArgumentValidator<T>
{
    private final @NonNull RangeValidator<T> rangeValidator;

    /**
     * Gets a minimum validator for integer values.
     *
     * @param minimum The lower limit (not inclusive!)
     * @return A new {@link MinimumValidator} for integer values.
     */
    public static MinimumValidator<Integer> integerMinimumValidator(final int minimum)
    {
        return new MinimumValidator<>(RangeValidator.integerRangeValidator(minimum, minimum));
    }

    /**
     * Gets a minimum validator for double values.
     *
     * @param minimum The lower limit (not inclusive!)
     * @return A new {@link MinimumValidator} for double values.
     */
    public static MinimumValidator<Double> doubleMinimumValidator(final double minimum)
    {
        return new MinimumValidator<>(RangeValidator.doubleRangeValidator(minimum, minimum));
    }

    @Override
    public boolean validate(final @Nullable T input)
    {
        if (input == null)
            return false;
        return rangeValidator.moreThan.apply(input, rangeValidator.min);
    }
}
