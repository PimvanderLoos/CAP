package nl.pim16aap2.commandparser.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.argumentparser.ArgumentParser;
import nl.pim16aap2.commandparser.argument.argumentparser.FlagParser;
import nl.pim16aap2.commandparser.argument.argumentparser.StringParser;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
@Getter
public abstract class Argument<T>
{
    protected final @NonNull String name;

    protected final @NonNull List<String> aliases;

    protected @NonNull String summary;

    protected @NonNull ArgumentParser<T> parser;

    @AllArgsConstructor
    @Getter
    public static class ParsedArgument<T>
    {
        @Nullable
        protected final T value;
    }

    public static class StringArgument
    {
        public static OptionalArgument.OptionalArgumentBuilder<String> getOptional()
        {
            return OptionalArgument.<String>builder().parser(StringParser.create());
        }

        public static RequiredArgument.RequiredArgumentBuilder<String> getRequired()
        {
            return RequiredArgument.<String>builder().parser(StringParser.create());
        }

        public static RepeatableArgument.RepeatableArgumentBuilder<List<String>, String> getRepeating()
        {
            return RepeatableArgument.<List<String>, String>builder().parser(StringParser.create());
        }
    }

    public static class FlagArgument
    {
        public static OptionalArgument.OptionalArgumentBuilder<Boolean> getOptional(final @NonNull Boolean value)
        {
            return OptionalArgument.<Boolean>builder().parser(FlagParser.create(value)).flag(true);
        }
    }
}
