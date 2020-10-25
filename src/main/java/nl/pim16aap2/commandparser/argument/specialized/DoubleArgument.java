package nl.pim16aap2.commandparser.argument.specialized;

import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.argument.RepeatableArgument;
import nl.pim16aap2.commandparser.argument.argumentparser.DoubleParser;
import nl.pim16aap2.commandparser.command.Command;

import java.util.List;

/**
 * Represents an argument that is parsed into a double.
 *
 * @author Pim
 */
public class DoubleArgument
{
    /**
     * Gets a builder for an optional {@link DoubleArgument}.
     *
     * @return A builder for an optional {@link DoubleArgument}.
     */
    public static Argument.OptionalBuilder<Double> getOptional()
    {
        return Argument.<Double>optionalBuilder().parser(DoubleParser.create());
    }

    /**
     * Gets a builder for a required {@link DoubleArgument}.
     * <p>
     * Note that single required arguments are always positional. Their position is defined by the order in which they
     * are added to a {@link Command}. Repeatable required arguments (see {@link #getRepeatable()} are not positional.
     *
     * @return A builder for a required {@link DoubleArgument}.
     */
    public static Argument.RequiredBuilder<Double> getRequired()
    {
        return Argument.<Double>requiredBuilder().parser(DoubleParser.create());
    }

    /**
     * Gets a builder for an optional positional {@link DoubleArgument}.
     * <p>
     * The position is defined by the order in which they are added to a {@link Command}.
     *
     * @return A builder for an optional {@link DoubleArgument}.
     */
    public static Argument.OptionalPositionalBuilder<Double> getOptionalPositional()
    {
        return Argument.<Double>optionalPositionalBuilder().parser(DoubleParser.create());
    }

    /**
     * Gets a builder for a repeatable {@link DoubleArgument}.
     * <p>
     * When set to required, at least 1 input argument will be required.
     * <p>
     * Unlike a regular required {@link Argument} (see {@link #getRequired()}) a repeatable required argument is not
     * positional.
     *
     * @return A builder for a repeatable {@link DoubleArgument}.
     */
    public static RepeatableArgument.RepeatableArgumentBuilder<List<Double>, Double> getRepeatable()
    {
        return RepeatableArgument.<List<Double>, Double>repeatableBuilder().parser(DoubleParser.create());
    }
}
