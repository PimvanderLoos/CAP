package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;

/**
 * Represents an argument parser for String values.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StringParser extends ArgumentParser<String>
{
    @Override
    public @NonNull String parseArgument(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                         final @NonNull Argument<?> argument, final @NonNull String value)
    {
        return value;
    }

    public static StringParser create()
    {
        return new StringParser();
    }
}
