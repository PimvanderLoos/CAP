package nl.pim16aap2.cap.argument.validator;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
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
     * @param cap           The {@link CAP} instance for which to validate the input.
     * @param commandSender The {@link ICommandSender} for which to validate the input.
     * @param argument      The {@link Argument} to validate the input for.
     * @param input         The input to validate.
     * @return True if the input is valid, otherwise false.
     *
     * @throws ValidationFailureException When the input variable is invalid.
     */
    void validate(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                  final @NonNull Argument<T> argument, final @Nullable T input)
        throws ValidationFailureException;
}
