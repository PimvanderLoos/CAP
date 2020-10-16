package nl.pim16aap2.commandparser.argument;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;
import nl.pim16aap2.commandparser.argument.argumentparser.ArgumentParser;

import java.util.List;

@Getter
public class RequiredArgument<T> extends Argument<T>
{
    @Setter
    private Integer position;

    private final T defaultValue;

    @Builder
    public RequiredArgument(final @NonNull String name, final @NonNull @Singular List<String> aliases,
                            final @NonNull String summary, final T defaultValue,
                            final @NonNull ArgumentParser<T> parser)
    {
        super(name, aliases, summary, parser);
        this.defaultValue = defaultValue;
    }
}
