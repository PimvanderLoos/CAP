package nl.pim16aap2.commandparser.argument;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@Getter
public class OptionalArgument<T> extends Argument<T>
{
    /**
     * Whether or not this is a flag argument. A flag argument doesn't take any values. Just their presence/absence is
     * the value.
     */
    private final Boolean flag;

    private final T defaultValue;

    @Builder
    public OptionalArgument(final @NonNull String name, final @NonNull @Singular List<String> aliases,
                            final @NonNull String summary, final T defaultValue, final @Nullable Boolean flag,
                            final @NonNull Function<@NonNull String, T> parser)
    {
        super(name, aliases, summary, parser);
        this.defaultValue = defaultValue;
        this.flag = Util.valOrDefault(flag, Boolean.FALSE);
    }
}
