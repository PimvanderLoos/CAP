package nl.pim16aap2.commandparser.argument;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.commandparser.argument.argumentparser.ArgumentParser;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

@Getter
public class RequiredArgument<T> extends Argument<T>
{
    @Setter
    private Integer position;

    private final T defaultValue;

    @Builder
    public RequiredArgument(final @NonNull String name, final @Nullable String longName, final @NonNull String summary,
                            final T defaultValue, final @NonNull ArgumentParser<T> parser)
    {
        super(name, Util.valOrDefault(longName, ""), summary, parser);
        this.defaultValue = defaultValue;
    }
}
