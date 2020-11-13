package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;

import java.text.MessageFormat;

/**
 * Represents an argument parser for integer values.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntegerParser extends ArgumentParser<Integer>
{
    @Override
    public @NonNull Integer parseArgument(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                          final @NonNull Argument<?> argument, final @NonNull String value)
        throws IllegalValueException
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            final @NonNull String localizedMessage =
                MessageFormat.format(cap.getMessage("error.valueParser.integer", commandSender), value);
            throw new IllegalValueException(argument, value, localizedMessage, cap.isDebug());
        }
    }

    public static IntegerParser create()
    {
        return new IntegerParser();
    }
}
