package nl.pim16aap2.commandparser.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Getter
public abstract class Argument<T>
{
    protected final @NonNull String name;

    protected final @NonNull List<String> aliases;

    protected @NonNull String summary;

    protected @NonNull Function<@NonNull String, T> parser;

    @AllArgsConstructor
    @Getter
    public static class ParsedArgument<T>
    {
        @Nullable
        protected final T value;
    }
}
