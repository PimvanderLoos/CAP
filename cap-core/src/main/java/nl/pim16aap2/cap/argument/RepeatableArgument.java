package nl.pim16aap2.cap.argument;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.parser.ArgumentParser;
import nl.pim16aap2.cap.argument.validator.IArgumentValidator;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RepeatableArgument<T> extends Argument<T>
{
    private static final boolean VALUE_LESS = false;
    private static final boolean REPEATABLE = true;
    private static final boolean POSITION = false;

    @Builder(builderMethodName = "repeatableBuilder")
    private RepeatableArgument(final @NonNull String shortName, final @Nullable String longName,
                               final @NonNull String summary, final @NonNull ArgumentParser<T> parser,
                               final @NonNull String label, final boolean required,
                               final @Nullable ITabCompleteFunction tabCompleteFunction,
                               final @Nullable IArgumentValidator<T> argumentValidator,
                               final @NonNull String identifier)
    {
        super(shortName, longName, summary, parser, null, label, VALUE_LESS, REPEATABLE, POSITION, required,
              tabCompleteFunction, argumentValidator, identifier);
    }

    @Override
    public @NonNull IParsedArgument<?> getParsedArgument(final @Nullable String value, final @NonNull CAP cap,
                                                         final @NonNull ICommandSender commandSender)
        throws ValidationFailureException, IllegalValueException
    {
        final ParsedRepeatableArgument<T> ret = new ParsedRepeatableArgument<>();
        if (value != null)
            ret.addValue(parseArgument(value, cap, commandSender));
        return ret;
    }

    @Override
    public @NonNull IParsedArgument<?> getDefault()
    {
        return new ParsedRepeatableArgument<T>();
    }

    /**
     * Represents an {@link IParsedArgument} for {@link RepeatableArgument}s. Unlike a regular {@link ParsedArgument},
     * this one stores the results in a list.
     *
     * @param <T> The type of the parsed arguments.
     */
    public static class ParsedRepeatableArgument<T> implements IParsedArgument<List<T>>
    {
        @Getter(onMethod = @__({@Override}))
        protected @NonNull List<T> value;

        private ParsedRepeatableArgument()
        {
            value = new ArrayList<>(0);
        }

        /**
         * Adds a value to the {@link List}.
         *
         * @param newValue The new value to add.
         */
        public void addValue(final @NonNull T newValue)
        {
            value.add(newValue);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> void updateValue(final @Nullable U newValue)
        {
            if (newValue == null)
                return;
            value.addAll((List<T>) newValue);
        }

        @Override
        public String toString()
        {
            return Util.listToString(value, Objects::toString);
        }
    }
}
