package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;

/**
 * Represents an argument parser for valueless parameters.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ValuelessParser<T extends Boolean> extends ArgumentParser<T>
{
    /**
     * Whether the presence of this argument flag should return true or false.
     */
    private final Boolean result;

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull T parseArgument(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                    final @NonNull Argument<?> argument, final @NonNull String value)
    {
        return (T) result;
    }

    public static ArgumentParser<Boolean> create(final @NonNull Boolean value)
    {
        return new ValuelessParser<>(value);
    }
}
