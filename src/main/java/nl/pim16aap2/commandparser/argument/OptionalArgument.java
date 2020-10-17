package nl.pim16aap2.commandparser.argument;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.argumentparser.ArgumentParser;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

@Getter
public class OptionalArgument<T> extends Argument<T>
{
    /**
     * Whether or not this is a flag argument. A flag argument doesn't take any values. Just their presence/absence is
     * the value.
     */
    private final Boolean flag;

    private final T defaultValue;

    @Builder(builderMethodName = "optionalBuilder")
    public OptionalArgument(final @NonNull String name, final @Nullable String longName, final @Nullable String summary,
                            final T defaultValue, final @Nullable Boolean flag, final @NonNull ArgumentParser<T> parser,
                            final @NonNull String label)
    {
        super(name, Util.valOrDefault(longName, ""), Util.valOrDefault(summary, ""), parser, label);
        this.defaultValue = defaultValue;
        this.flag = Util.valOrDefault(flag, Boolean.FALSE);
    }
}
