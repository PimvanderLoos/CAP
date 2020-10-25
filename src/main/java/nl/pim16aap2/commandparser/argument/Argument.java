package nl.pim16aap2.commandparser.argument;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.argumentparser.ArgumentParser;
import nl.pim16aap2.commandparser.argument.argumentparser.FlagParser;
import nl.pim16aap2.commandparser.argument.argumentparser.StringParser;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@Getter
public class Argument<T>
{
    protected final @NonNull String name;

    protected final @Nullable String longName;

    protected @NonNull String summary;

    @Getter(AccessLevel.PROTECTED)
    protected @NonNull ArgumentParser<T> parser;

    protected final @NonNull String label;

    protected final @Nullable T defaultValue;

    protected final boolean repeatable;

    protected final boolean valuesLess;

    protected final int position;

    protected final boolean required;

    protected final @Nullable Supplier<List<String>> tabcompleteFunction;

    protected Argument(final @NonNull String name, final @Nullable String longName, final @NonNull String summary,
                       final @NonNull ArgumentParser<T> parser, final @Nullable T defaultValue,
                       final @NonNull String label, final boolean valuesLess, final boolean repeatable,
                       final int position, final boolean required,
                       final @Nullable Supplier<List<String>> tabcompleteFunction)
    {
        this.name = name;
        this.longName = longName;
        this.summary = summary;
        this.parser = parser;
        this.defaultValue = defaultValue;
        this.label = label;
        this.valuesLess = valuesLess;
        this.repeatable = repeatable;
        this.position = position;
        this.required = required;
        this.tabcompleteFunction = tabcompleteFunction;
    }

    @Builder(builderMethodName = "requiredBuilder", builderClassName = "RequiredBuilder")
    protected Argument(final @NonNull String name, final @Nullable String longName, final @NonNull String summary,
                       final @NonNull ArgumentParser<T> parser, final int position,
                       final @Nullable Supplier<List<String>> tabcompleteFunction)
    {
        this(name, longName, summary, parser, null, "", false, false, position, true, tabcompleteFunction);
    }

    @Builder(builderMethodName = "optionalPositionalBuilder", builderClassName = "OptionalPositionalBuilder")
    protected Argument(final @NonNull String name, final @NonNull String longName, final @NonNull String summary,
                       final int position, final @NonNull ArgumentParser<T> parser,
                       final @Nullable Supplier<List<String>> tabcompleteFunction)
    {
        this(name, longName, summary, parser, null, "", false, false, position, false, tabcompleteFunction);
    }

    @Builder(builderMethodName = "optionalBuilder", builderClassName = "OptionalBuilder")
    protected Argument(final @NonNull String name, final @Nullable String longName, final @NonNull String summary,
                       final @NonNull ArgumentParser<T> parser, final @Nullable T defaultValue,
                       final @NonNull String label,
                       final @Nullable Supplier<List<String>> tabcompleteFunction)
    {
        this(name, longName, summary, parser, defaultValue, label, false, false, -1, false, tabcompleteFunction);
    }

    @SuppressWarnings("unchecked")
    @Builder(builderMethodName = "privateValuesLessBuilder", builderClassName = "ValuesLessBuilder")
    protected Argument(final @NonNull String name, final @Nullable String longName, final @NonNull String summary,
                       final @Nullable Boolean value)
    {
        this(name, longName, summary,
             (ArgumentParser<T>) FlagParser.create(Util.valOrDefault(value, Boolean.TRUE)),
             (T) (Boolean) (!Util.valOrDefault(value, Boolean.TRUE)), "", true, false, -1, false, null);
    }

    private static <T> ValuesLessBuilder<T> privateValuesLessBuilder()
    {
        return new ValuesLessBuilder<T>();
    }

    public static @NonNull ValuesLessBuilder<Boolean> valuesLessBuilder()
    {
        return Argument.privateValuesLessBuilder();
    }

    public @NonNull IParsedArgument<?> parseArgument(final @Nullable String value)
    {
        if (value == null)
            return new ParsedArgument<>(defaultValue);
        return new ParsedArgument<>(parser.parseArgument(value));
    }
    
    public @NonNull IParsedArgument<?> getDefault()
    {
        return new ParsedArgument<>(defaultValue);
    }

    public boolean isPositional()
    {
        return position >= 0;
    }

    public interface IParsedArgument<T>
    {
        /**
         * Gets the value as it was parsed from the arguments.
         *
         * @return The parsed value.
         */
        @Nullable
        T getValue();

        /**
         * Updates the currently stored value with a new value.
         *
         * @param value The new value to store.
         */
        <U> void updateValue(final @Nullable U value);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ParsedArgument<T> implements IParsedArgument<T>
    {
        @Getter(onMethod = @__({@Override}))
        protected @Nullable T value;

        @Override
        @SuppressWarnings("unchecked")
        public <U> void updateValue(final @Nullable U newValue)
        {
            value = (T) newValue;
        }
    }

    public static class StringArgument
    {
        public static Argument.OptionalBuilder<String> getOptional()
        {
            return Argument.<String>optionalBuilder().parser(StringParser.create());
        }

        public static Argument.RequiredBuilder<String> getRequired()
        {
            return Argument.<String>requiredBuilder().parser(StringParser.create());
        }

        public static Argument.OptionalPositionalBuilder<String> getOptionalPositional()
        {
            return Argument.<String>optionalPositionalBuilder().parser(StringParser.create());
        }

        public static RepeatableArgument.RepeatableArgumentBuilder<List<String>, String> getRepeatable()
        {
            return RepeatableArgument.<List<String>, String>repeatableBuilder().parser(StringParser.create());
        }
    }
}
