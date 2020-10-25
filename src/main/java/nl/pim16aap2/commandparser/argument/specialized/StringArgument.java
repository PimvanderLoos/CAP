package nl.pim16aap2.commandparser.argument.specialized;


import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.argument.RepeatableArgument;
import nl.pim16aap2.commandparser.argument.argumentparser.StringParser;
import nl.pim16aap2.commandparser.command.Command;

import java.util.List;

/**
 * Represents an argument that is parsed into a String.
 *
 * @author Pim
 */
public class StringArgument
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
     * Note that single required arguments are always positional. Their position is defined by the order in which they
     * are added to a {@link Command}. Repeatable required arguments (see {@link #getRepeatable()} are not positional.
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
