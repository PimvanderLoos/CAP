package nl.pim16aap2.commandparser.argument;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.argumentparser.ArgumentParser;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RepeatableArgument<T extends List<U>, U> extends Argument<U>
{
    @Builder(builderMethodName = "repeatableBuilder")
    private RepeatableArgument(final @NonNull String name, final @Nullable String longName,
                               final @NonNull String summary, final @NonNull ArgumentParser<U> parser,
                               final @NonNull String label, final boolean required,
                               final @Nullable Supplier<List<String>> tabcompleteFunction)
    {
        super(name, longName, summary, parser, null, label, false, true, -1, required, tabcompleteFunction);
    }

    @Override
    public @NonNull IParsedArgument<?> parseArgument(final @Nullable String value)
    {
        final ParsedRepeatableArgument<T, U> ret = new ParsedRepeatableArgument<>();
        if (value != null)
            ret.addValue(parser.parseArgument(value));
        return ret;
    }

    @Override
    public @NonNull IParsedArgument<?> getDefault()
    {
        return new ParsedRepeatableArgument<T, U>();
    }

    public static class ParsedRepeatableArgument<T extends List<U>, U> implements IParsedArgument<T>
    {
        @Getter(onMethod = @__({@Override}))
        protected @NonNull T value;

        @SuppressWarnings("unchecked")
        private ParsedRepeatableArgument()
        {
            value = ((T) new ArrayList<U>(0));
        }

        public void addValue(final @NonNull U newValue)
        {
            value.add(newValue);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> void updateValue(final @Nullable V newValue)
        {
            if (newValue == null)
                return;
            value.addAll((T) newValue);
        }
    }
}
