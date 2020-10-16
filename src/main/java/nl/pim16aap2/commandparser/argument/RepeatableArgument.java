package nl.pim16aap2.commandparser.argument;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.argumentparser.ArgumentParser;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class RepeatableArgument<T extends List<U>, U> extends OptionalArgument<U>
{
    /**
     * Whether or not this is a flag argument. A flag argument doesn't take any values. Just their presence/absence is
     * the value.
     */
    private final Boolean flag;

    @Builder(builderMethodName = "repeatableBuilder")
    public RepeatableArgument(final @NonNull String name, final @Nullable String longName,
                              final @Nullable String summary, final @Nullable Boolean flag,
                              final @NonNull ArgumentParser<U> parser)
    {
        super(name, longName, summary, null, false, parser);
        this.flag = Util.valOrDefault(flag, Boolean.FALSE);
    }


    // TODO: These should implement an interface or something. Inheritance doesn't make any sense.
    @Getter
    public static class ParsedRepeatableArgument<T extends List<U>, U> extends ParsedArgument<T>
    {
        public ParsedRepeatableArgument(final @NonNull T value)
        {
            super(value);
        }

        @SuppressWarnings("unchecked")
        public ParsedRepeatableArgument()
        {
            this((T) new ArrayList<U>(0));
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NonNull T getValue()
        {
            return value == null ? (T) Collections.emptyList() : value;
        }

        @SuppressWarnings("unchecked")
        public void addValue(final RepeatableArgument<? extends List<?>, ?> repeatableArgument,
                             final @NonNull String value)
        {
            Objects.requireNonNull(super.value).add((U) repeatableArgument.parser.parseArgument(value));
        }
    }
}
