package nl.pim16aap2.cap.argument.specialized;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.RepeatableArgument;
import nl.pim16aap2.cap.argument.parser.ArgumentParser;
import nl.pim16aap2.cap.command.Command;

/**
 * Represents a specialized type of {@link Argument}.
 *
 * @param <T> The specialized type.
 * @author Pim+
 */
@RequiredArgsConstructor
public abstract class SpecializedArgument<T>
{
    /**
     * The {@link ArgumentParser} that is used to parse the input value (String) into the desired output value.
     */
    protected final @NonNull ArgumentParser<T> parser;

    /**
     * Gets a builder for an optional {@link Argument}.
     *
     * @return A builder for an optional {@link Argument}.
     */
    public Argument.OptionalBuilder<T> getOptional()
    {
        return Argument.<T>optionalBuilder().parser(parser);
    }

    /**
     * Gets a builder for a required {@link Argument}.
     * <p>
     * Note that single required arguments are always positional. Their position is defined by the order in which they
     * are added to a {@link Command}. Repeatable required arguments (see {@link #getRepeatable()} are not positional.
     *
     * @return A builder for a required {@link Argument}.
     */
    public @NonNull Argument.RequiredBuilder<T> getRequired()
    {
        return Argument.<T>requiredBuilder().parser(parser);
    }

    /**
     * Gets a builder for an optional positional {@link Argument}.
     * <p>
     * The position is defined by the order in which they are added to a {@link Command}.
     *
     * @return A builder for an optional {@link Argument}.
     */
    public @NonNull Argument.OptionalPositionalBuilder<T> getOptionalPositional()
    {
        return Argument.<T>optionalPositionalBuilder().parser(parser);
    }

    /**
     * Gets a builder for a repeatable {@link Argument}.
     * <p>
     * When set to required, at least 1 input argument will be required.
     * <p>
     * Unlike a regular required {@link Argument} (see {@link #getRequired()}) a repeatable required argument is not
     * positional.
     *
     * @return A builder for a repeatable {@link Argument}.
     */
    public @NonNull RepeatableArgument.RepeatableArgumentBuilder<T> getRepeatable()
    {
        return RepeatableArgument.<T>repeatableBuilder().parser(parser);
    }
}
