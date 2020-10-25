package nl.pim16aap2.commandparser.argument.specialized;

import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.argument.RepeatableArgument;
import nl.pim16aap2.commandparser.argument.argumentparser.IntegerParser;
import nl.pim16aap2.commandparser.command.Command;

import java.util.List;

/**
 * Represents an argument that is parsed into an integer.
 *
 * @author Pim
 */
public class IntegerArgument
{
    /**
     * Gets a builder for an optional {@link IntegerArgument}.
     *
     * @return A builder for an optional {@link IntegerArgument}.
     */
    public static Argument.OptionalBuilder<Integer> getOptional()
    {
        return Argument.<Integer>optionalBuilder().parser(IntegerParser.create());
    }

    /**
     * Gets a builder for a required {@link IntegerArgument}.
     * <p>
     * Note that single required arguments are always positional. Their position is defined by the order in which they
     * are added to a {@link Command}. Repeatable required arguments (see {@link #getRepeatable()} are not positional.
     *
     * @return A builder for a required {@link IntegerArgument}.
     */
    public static Argument.RequiredBuilder<Integer> getRequired()
    {
        return Argument.<Integer>requiredBuilder().parser(IntegerParser.create());
    }

    /**
     * Gets a builder for an optional positional {@link IntegerArgument}.
     * <p>
     * The position is defined by the order in which they are added to a {@link Command}.
     *
     * @return A builder for an optional {@link IntegerArgument}.
     */
    public static Argument.OptionalPositionalBuilder<Integer> getOptionalPositional()
    {
        return Argument.<Integer>optionalPositionalBuilder().parser(IntegerParser.create());
    }

    /**
     * Gets a builder for a repeatable {@link IntegerArgument}.
     * <p>
     * When set to required, at least 1 input argument will be required.
     * <p>
     * Unlike a regular required {@link Argument} (see {@link #getRequired()}) a repeatable required argument is not
     * positional.
     *
     * @return A builder for a repeatable {@link IntegerArgument}.
     */
    public static RepeatableArgument.RepeatableArgumentBuilder<List<Integer>, Integer> getRepeatable()
    {
        return RepeatableArgument.<List<Integer>, Integer>repeatableBuilder().parser(IntegerParser.create());
    }
}
