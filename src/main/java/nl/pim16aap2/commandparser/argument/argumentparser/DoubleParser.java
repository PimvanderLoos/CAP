package nl.pim16aap2.commandparser.argument.argumentparser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DoubleParser extends ArgumentParser<Double>
{
    @Override
    public @NonNull Double parseArgument(final @NonNull String value)
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Input \"" + value + "\" cannot be parsed into a double!");
        }
    }

    public static DoubleParser create()
    {
        return new DoubleParser();
    }
}
