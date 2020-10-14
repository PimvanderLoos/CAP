package nl.pim16aap2.commandparser.commandline.argument;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;

import java.util.List;
import java.util.function.Function;

@Getter
public class RequiredArgument<T> extends Argument<T>
{
    @Setter
    private Integer position;

    @Builder
    public RequiredArgument(final @NonNull String name, final @NonNull @Singular List<String> aliases,
                            final @NonNull String summary, final T defautValue,
                            final @NonNull Function<@NonNull String, ParsedArgument<T>> parser)
    {
        super(name, aliases, summary, defautValue, parser);
    }


}
