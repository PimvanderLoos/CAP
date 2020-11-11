package nl.pim16aap2.cap.argument.validator.number;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaximumValidatorTest
{
    public static final @NonNull ICommandSender commandSender = new DefaultCommandSender();
    public static final @NonNull Argument<?> argument = Argument.valuesLessBuilder().shortName("a").summary("").build();
    public static final @NonNull CAP cap = CAP.getDefault();

    @Test
    void validateRangeInteger()
    {
        final @NonNull MaximumValidator<Integer> maximumValidator = MaximumValidator.integerMaximumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> maximumValidator.validate(cap, commandSender, argument, 10));

        Assertions.assertDoesNotThrow(() -> maximumValidator.validate(cap, commandSender, argument, 9));
    }

    @Test
    void validateRangeDouble()
    {
        final @NonNull MaximumValidator<Double> maximumValidator = MaximumValidator.doubleMaximumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> maximumValidator.validate(cap, commandSender, argument, 10.0));

        Assertions.assertDoesNotThrow(() -> maximumValidator.validate(cap, commandSender, argument, 9.9));
    }

    /**
     * A {@link RangeValidator.ValueRequest} that returns a specific value.
     *
     * @param ret The value to return.
     * @return The specified value.
     */
    private @NonNull Integer maximumSupplier(final @NonNull Integer ret, final @NonNull CAP cap,
                                             final @NonNull ICommandSender commandSender, @NonNull Argument<?> argument)
    {
        return ret;
    }

    @Test
    void validateDynamic()
    {
        final @NonNull Integer maximum = 10;
        final @NonNull MaximumValidator<Integer> maximumValidator = MaximumValidator.integerMaximumValidator(
            (cap1, commandSender1, argument1) -> maximumSupplier(maximum, cap1, commandSender1, argument1));

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> maximumValidator.validate(cap, commandSender, argument, maximum));

        Assertions.assertDoesNotThrow(() -> maximumValidator.validate(cap, commandSender, argument, maximum - 1));
    }
}