package nl.pim16aap2.cap.argument.validator;

import org.jetbrains.annotations.Nullable;

/**
 * Provides a way to validation arguments. For example by making sure that integer values are in a specific range.
 *
 * @param <T> The type of the input to validate.
 * @author Pim
 */
public interface IArgumentValidator<T>
{
    /**
     * Validates input.
     *
     * @param input The input to validate.
     * @return True if the input is valid, otherwise false.
     */
    boolean validate(final @Nullable T input);
}
