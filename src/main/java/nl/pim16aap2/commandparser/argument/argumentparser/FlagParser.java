package nl.pim16aap2.commandparser.argument.argumentparser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FlagParser<T extends Boolean> extends ArgumentParser<T>
{
    /**
     * Whether the presence of this argument flag should return true or false.
     */
    private final Boolean result;

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull T parseArgument(final @NonNull String value)
    {
        return (T) result;
    }

    public static ArgumentParser<Boolean> create(final @NonNull Boolean value)
    {
        return new FlagParser<>(value);
    }
}
