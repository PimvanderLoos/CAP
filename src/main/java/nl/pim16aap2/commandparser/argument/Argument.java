package nl.pim16aap2.commandparser.argument;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.argumentparser.ArgumentParser;
import nl.pim16aap2.commandparser.argument.argumentparser.FlagParser;
import nl.pim16aap2.commandparser.argument.argumentparser.StringParser;
import nl.pim16aap2.commandparser.argument.validator.IArgumentValidator;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.exception.ValidationFailureException;
import nl.pim16aap2.commandparser.manager.CommandManager;
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

    protected final @Nullable IArgumentValidator<T> argumentValidator;

    protected Argument(final @NonNull String name, final @Nullable String longName, final @NonNull String summary,
                       final @NonNull ArgumentParser<T> parser, final @Nullable T defaultValue,
                       final @NonNull String label, final boolean valuesLess, final boolean repeatable,
                       final int position, final boolean required,
                       final @Nullable Supplier<List<String>> tabcompleteFunction,
                       final @Nullable IArgumentValidator<T> argumentValidator)
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
        this.argumentValidator = argumentValidator;
    }

    @Builder(builderMethodName = "requiredBuilder", builderClassName = "RequiredBuilder")
    protected Argument(final @NonNull String name, final @Nullable String longName, final @NonNull String summary,
                       final @NonNull ArgumentParser<T> parser, final int position,
                       final @Nullable Supplier<List<String>> tabcompleteFunction,
                       final @Nullable IArgumentValidator<T> argumentValidator)
    {
        this(name, longName, summary, parser, null, "", false, false, position, true, tabcompleteFunction,
             argumentValidator);
    }

    @Builder(builderMethodName = "optionalPositionalBuilder", builderClassName = "OptionalPositionalBuilder")
    protected Argument(final @NonNull String name, final @NonNull String longName, final @NonNull String summary,
                       final int position, final @NonNull ArgumentParser<T> parser,
                       final @Nullable Supplier<List<String>> tabcompleteFunction,
                       final @Nullable IArgumentValidator<T> argumentValidator)
    {
        this(name, longName, summary, parser, null, "", false, false, position, false, tabcompleteFunction,
             argumentValidator);
    }

    @Builder(builderMethodName = "optionalBuilder", builderClassName = "OptionalBuilder")
    protected Argument(final @NonNull String name, final @Nullable String longName, final @NonNull String summary,
                       final @NonNull ArgumentParser<T> parser, final @Nullable T defaultValue,
                       final @NonNull String label, final @Nullable Supplier<List<String>> tabcompleteFunction,
                       final @Nullable IArgumentValidator<T> argumentValidator)
    {
        this(name, longName, summary, parser, defaultValue, label, false, false, -1, false, tabcompleteFunction,
             argumentValidator);
    }

    @SuppressWarnings("unchecked")
    @Builder(builderMethodName = "privateValuesLessBuilder", builderClassName = "ValuesLessBuilder")
    protected Argument(final @NonNull String name, final @Nullable String longName, final @NonNull String summary,
                       final @Nullable Boolean value)
    {
        this(name, longName, summary,
             (ArgumentParser<T>) FlagParser.create(Util.valOrDefault(value, Boolean.TRUE)),
             (T) (Boolean) (!Util.valOrDefault(value, Boolean.TRUE)), "", true, false, -1, false, null, null);
    }

    private static <T> ValuesLessBuilder<T> privateValuesLessBuilder()
    {
        return new ValuesLessBuilder<T>();
    }

    public static @NonNull ValuesLessBuilder<Boolean> valuesLessBuilder()
    {
        return Argument.privateValuesLessBuilder();
    }

    /**
     * Parses the input using {@link #parser} and validates it using the {@link #argumentValidator} if it is provided.
     *
     * @param value          The value to parse and validate.
     * @param commandManager The {@link CommandManager} that requested the argument to be parsed.
     * @return The parsed value.
     *
     * @throws ValidationFailureException If the value was not valid. See {@link IArgumentValidator#validate(Object)}.
     */
    @Nullable
    protected T parseArgument(final @NonNull String value, final @NonNull CommandManager commandManager)
        throws ValidationFailureException
    {
        final T parsed = parser.parseArgument(value);

        if (argumentValidator != null && !argumentValidator.validate(parsed))
            throw new ValidationFailureException(this, value, commandManager.isDebug());

        return parsed;
    }

    /**
     * Parses the input and stores the result in an {@link IParsedArgument}. See {@link #parseArgument(String,
     * CommandManager)}.
     */
    public @NonNull IParsedArgument<?> getParsedArgument(final @Nullable String value,
                                                         final @NonNull CommandManager commandManager)
        throws ValidationFailureException
    {
        if (value == null)
            return new ParsedArgument<>(defaultValue);
        return new ParsedArgument<>(parseArgument(value, commandManager));
    }

    /**
     * Gets a {@link IParsedArgument} using the {@link #defaultValue}.
     *
     * @return A {@link IParsedArgument} using the {@link #defaultValue}.
     */
    public @NonNull IParsedArgument<?> getDefault()
    {
        return new ParsedArgument<>(defaultValue);
    }

    /**
     * Checks if this {@link Argument} is positional or not.
     *
     * @return True if this {@link Argument} is positional.
     */
    public boolean isPositional()
    {
        return position >= 0;
    }

    /**
     * Represents the results of parsing an argument.
     *
     * @param <T> The type of the parsed result.
     * @author Pim
     */
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

    /**
     * The implementation of {@link IParsedArgument} for regular {@link Argument}s.
     *
     * @param <T> The type of the parsed result.
     * @author Pim
     */
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

    /**
     * Represents an argument that is parsed into a String.
     *
     * @author Pim
     */
    public static class StringArgument
    {
        /**
         * Gets a builder for an optional {@link StringArgument}.
         *
         * @return A builder for an optional {@link StringArgument}.
         */
        public static Argument.OptionalBuilder<String> getOptional()
        {
            return Argument.<String>optionalBuilder().parser(StringParser.create());
        }

        /**
         * Gets a builder for a required {@link StringArgument}.
         * <p>
         * Note that single required arguments are always positional. Their position is defined by the order in which
         * they are added to a {@link Command}. Repeatable required arguments (see {@link #getRepeatable()} are not
         * positional.
         *
         * @return A builder for a required {@link StringArgument}.
         */
        public static Argument.RequiredBuilder<String> getRequired()
        {
            return Argument.<String>requiredBuilder().parser(StringParser.create());
        }

        /**
         * Gets a builder for an optional positional {@link StringArgument}.
         * <p>
         * The position is defined by the order in which they are added to a {@link Command}.
         *
         * @return A builder for an optional {@link StringArgument}.
         */
        public static Argument.OptionalPositionalBuilder<String> getOptionalPositional()
        {
            return Argument.<String>optionalPositionalBuilder().parser(StringParser.create());
        }

        /**
         * Gets a builder for a repeatable {@link StringArgument}.
         * <p>
         * When set to required, at least 1 input argument will be required.
         * <p>
         * Unlike a regular required {@link Argument} (see {@link #getRequired()}) a repeatable required argument is not
         * positional.
         *
         * @return A builder for a repeatable {@link StringArgument}.
         */
        public static RepeatableArgument.RepeatableArgumentBuilder<List<String>, String> getRepeatable()
        {
            return RepeatableArgument.<List<String>, String>repeatableBuilder().parser(StringParser.create());
        }
    }
}
