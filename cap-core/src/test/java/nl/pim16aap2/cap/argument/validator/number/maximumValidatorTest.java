package nl.pim16aap2.cap.argument.validator.number;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class maximumValidatorTest
{
    @Test
    void validateInteger()
    {
        final @NonNull MaximumValidator<Integer> maximumValidator = MaximumValidator.integerMaximumValidator(10);
        Assertions.assertTrue(maximumValidator.validate(9));
        Assertions.assertFalse(maximumValidator.validate(10));
    }

    @Test
    void validateDouble()
    {
        final @NonNull MaximumValidator<Double> maximumValidator = MaximumValidator.doubleMaximumValidator(10);
        Assertions.assertTrue(maximumValidator.validate(9.9));
        Assertions.assertFalse(maximumValidator.validate(10.0));
    }
}
