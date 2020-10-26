package nl.pim16aap2.cap.argument.validator.number;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MinimumValidatorTest
{
    @Test
    void validateInteger()
    {
        final @NonNull MinimumValidator<Integer> minimumValidator = MinimumValidator.integerMinimumValidator(10);
        Assertions.assertFalse(minimumValidator.validate(10));
        Assertions.assertTrue(minimumValidator.validate(11));
    }

    @Test
    void validateDouble()
    {
        final @NonNull MinimumValidator<Double> minimumValidator = MinimumValidator.doubleMinimumValidator(10);
        Assertions.assertFalse(minimumValidator.validate(10.0));
        Assertions.assertTrue(minimumValidator.validate(11.0));
    }
}
