package nl.pim16aap2.cap.argument.parser;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;

/**
 * Represents an object that can parse a String into a desired type.
 *
 * @param <T> The type of the result to parse the String into.
 * @author Pim
 */
public abstract class ArgumentParser<T>
{
    public abstract @NonNull T parseArgument(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                             final @NonNull Argument<?> argument, final @NonNull String value)
        throws IllegalValueException;
}
