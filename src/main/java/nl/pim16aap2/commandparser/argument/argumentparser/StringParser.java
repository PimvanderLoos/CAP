package nl.pim16aap2.commandparser.argument.argumentparser;

import lombok.NonNull;

import static nl.pim16aap2.commandparser.argument.Argument.ParsedArgument;

public class StringParser extends ArgumentParser<String>
{
    @Override
    public @NonNull ParsedArgument<String> parseArgument(final @NonNull String value)
    {
        return new ParsedArgument<>(value);
    }
}
