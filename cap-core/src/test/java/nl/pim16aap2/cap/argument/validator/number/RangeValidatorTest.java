package nl.pim16aap2.cap.argument.validator.number;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RangeValidatorTest
{
    @Test
    void validateInteger()
    {
        final @NonNull RangeValidator<Integer> rangeValidator = RangeValidator.integerRangeValidator(10, 20);
        Assertions.assertFalse(rangeValidator.validate(10));
        Assertions.assertTrue(rangeValidator.validate(11));
        Assertions.assertFalse(rangeValidator.validate(20));
    }

    @Test
    void validateDouble()
    {
        final @NonNull RangeValidator<Double> rangeValidator = RangeValidator.doubleRangeValidator(10, 20);
        Assertions.assertFalse(rangeValidator.validate(10.0));
        Assertions.assertTrue(rangeValidator.validate(11.0));
        Assertions.assertFalse(rangeValidator.validate(20.0));
    }
}
