package nl.pim16aap2.cap.argument.validator.number;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static nl.pim16aap2.cap.util.UtilsForTesting.*;

class MinimumValidatorTest
{
    @Test
    void validateRangeInteger()
    {
        final @NonNull MinimumValidator<Integer> minimumValidator = MinimumValidator.integerMinimumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class, () ->
            minimumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 9));

        Assertions.assertDoesNotThrow(
            () -> minimumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 10));
        Assertions.assertDoesNotThrow(
            () -> minimumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 11));
    }

    @Test
    void validateRangeDouble()
    {
        final @NonNull MinimumValidator<Double> minimumValidator = MinimumValidator.doubleMinimumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class, () ->
            minimumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 9.9));

        Assertions.assertDoesNotThrow(
            () -> minimumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 10.0));
        Assertions.assertDoesNotThrow(
            () -> minimumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 11.0));
    }

    /**
     * A {@link RangeValidator.ValueRequest} that returns a specific value.
     *
     * @param ret The value to return.
     * @return The specified value.
     */
    private @NonNull Integer minimumSupplier(final @NonNull Integer ret, final @NonNull CAP cap,
                                             final @NonNull ICommandSender commandSender, @NonNull Argument<?> argument)
    {
        return ret;
    }

    @Test
    void validateDynamic()
    {
        final int minimum = 10;
        final @NonNull MinimumValidator<Integer> minimumSupplier = MinimumValidator.integerMinimumValidator(
            (cap1, commandSender1, argument1) -> minimumSupplier(minimum, cap1, commandSender1, argument1));

        @NonNull ValidationFailureException exception =
            Assertions.assertThrows(ValidationFailureException.class, () ->
                minimumSupplier.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, minimum - 1));
        Assertions.assertEquals("Value 9 should be more than 10!", exception.getLocalizedMessage());

        Assertions.assertDoesNotThrow(
            () -> minimumSupplier.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, minimum));
        Assertions.assertDoesNotThrow(
            () -> minimumSupplier.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, minimum + 1));
    }
}
