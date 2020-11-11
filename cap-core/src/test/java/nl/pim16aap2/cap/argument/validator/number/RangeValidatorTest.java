package nl.pim16aap2.cap.argument.validator.number;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RangeValidatorTest
{
    public static final @NonNull ICommandSender commandSender = new DefaultCommandSender();
    public static final @NonNull Argument<?> argument = Argument.valuesLessBuilder().shortName("a").summary("").build();
    public static final @NonNull CAP cap = CAP.getDefault();

    @Test
    void validateRangeInteger()
    {
        final @NonNull RangeValidator<Integer> rangeValidator = RangeValidator.integerRangeValidator(10, 20);

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> rangeValidator.validate(cap, commandSender, argument, 10));

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> rangeValidator.validate(cap, commandSender, argument, 20));

        Assertions.assertDoesNotThrow(() -> rangeValidator.validate(cap, commandSender, argument, 11));
    }

    @Test
    void validateRangeDouble()
    {
        final @NonNull RangeValidator<Double> rangeValidator = RangeValidator.doubleRangeValidator(10, 20);

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> rangeValidator.validate(cap, commandSender, argument, 10.0));

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> rangeValidator.validate(cap, commandSender, argument, 20.0));

        Assertions.assertDoesNotThrow(() -> rangeValidator.validate(cap, commandSender, argument, 11.0));
    }

    /**
     * A {@link RangeValidator.ValueRequest} that returns a specific value.
     *
     * @param ret The value to return.
     * @return The specified value.
     */
    private @NonNull Integer valueSupplier(final @NonNull Integer ret, final @NonNull CAP cap,
                                           final @NonNull ICommandSender commandSender, @NonNull Argument<?> argument)
    {
        return ret;
    }

    @Test
    void validateDynamic()
    {
        final @NonNull Integer minimum = 10;
        final @NonNull Integer maximum = 20;
        final @NonNull RangeValidator<Integer> rangeValidator = RangeValidator.integerRangeValidator(
            (cap1, commandSender1, argument1) -> valueSupplier(minimum, cap1, commandSender1, argument1),
            (cap1, commandSender1, argument1) -> valueSupplier(maximum, cap1, commandSender1, argument1));

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> rangeValidator.validate(cap, commandSender, argument, maximum));

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> rangeValidator.validate(cap, commandSender, argument, minimum));

        Assertions.assertDoesNotThrow(() -> rangeValidator.validate(cap, commandSender, argument, maximum - 1));
    }
}
