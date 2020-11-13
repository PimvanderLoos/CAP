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
 * Represents an argument parser for double values.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DoubleParser extends ArgumentParser<Double>
{
    @Override
    public @NonNull Double parseArgument(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                         final @NonNull Argument<?> argument, final @NonNull String value)
        throws IllegalValueException
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e)
        {
            final @NonNull String localizedMessage =
                MessageFormat.format(cap.getMessage("error.valueParser.double", commandSender), value);
            throw new IllegalValueException(argument, value, localizedMessage, cap.isDebug());
        }
    }

    public static DoubleParser create()
    {
        return new DoubleParser();
    }
}
