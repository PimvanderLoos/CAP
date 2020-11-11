package nl.pim16aap2.cap.argument.validator.number;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RangeValidatorTest
{
    @Test
    void validateRangeInteger()
    {
        final @NonNull RangeValidator<Integer> rangeValidator = RangeValidator.integerRangeValidator(10, 20);
        Assertions.assertFalse(rangeValidator.inRange(10));
        Assertions.assertTrue(rangeValidator.inRange(11));
        Assertions.assertFalse(rangeValidator.inRange(20));
    }

    @Test
    void validateRangeDouble()
    {
        final @NonNull RangeValidator<Double> rangeValidator = RangeValidator.doubleRangeValidator(10, 20);
        Assertions.assertFalse(rangeValidator.inRange(10.0));
        Assertions.assertTrue(rangeValidator.inRange(11.0));
        Assertions.assertFalse(rangeValidator.inRange(20.0));
    }

    @Test
    void validateMinimumInteger()
    {
        final @NonNull RangeValidator<Integer> rangeValidator = RangeValidator.integerRangeValidator(10, 2000);
        Assertions.assertFalse(rangeValidator.moreThanMin(10));
        Assertions.assertTrue(rangeValidator.moreThanMin(11));
    }

    @Test
    void validateMinimumDouble()
    {
        final @NonNull RangeValidator<Double> rangeValidator = RangeValidator.doubleRangeValidator(10, 9999);
        Assertions.assertFalse(rangeValidator.moreThanMin(10.0));
        Assertions.assertTrue(rangeValidator.moreThanMin(11.0));
    }

    @Test
    void validateMaximumInteger()
    {
        final @NonNull RangeValidator<Integer> rangeValidator = RangeValidator.integerRangeValidator(0, 10);
        Assertions.assertTrue(rangeValidator.lessThanMax(9));
        Assertions.assertFalse(rangeValidator.lessThanMax(10));
    }

    @Test
    void validateMaximumDouble()
    {
        final @NonNull RangeValidator<Double> rangeValidator = RangeValidator.doubleRangeValidator(0, 10);
        Assertions.assertTrue(rangeValidator.lessThanMax(9.9));
        Assertions.assertFalse(rangeValidator.lessThanMax(10.0));
    }
}
