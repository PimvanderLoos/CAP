package nl.pim16aap2.cap.argument.validator.number;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static nl.pim16aap2.cap.util.UtilsForTesting.*;

class MaximumValidatorTest
{
    @Test
    void validateRangeInteger()
    {
        final @NonNull MaximumValidator<Integer> maximumValidator = MaximumValidator.integerMaximumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class, () ->
            maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 11));

        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 10));
        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 9));
    }

    @Test
    void validateRangeDouble()
    {
        final @NonNull MaximumValidator<Double> maximumValidator = MaximumValidator.doubleMaximumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class, () ->
            maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 10.1));

        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 9.9));
        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 10.0));
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
        final int maximum = 10;
        final @NonNull MaximumValidator<Integer> maximumValidator = MaximumValidator.integerMaximumValidator(
            (cap1, commandSender1, argument1) -> maximumSupplier(maximum, cap1, commandSender1, argument1));

        @NonNull ValidationFailureException exception =
            Assertions.assertThrows(ValidationFailureException.class, () ->
                maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, maximum + 1));
        Assertions.assertEquals("Value 11 should be less than 10!", exception.getLocalizedMessage());

        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, maximum));
        Assertions
            .assertDoesNotThrow(
                () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, maximum - 1));
    }
}
