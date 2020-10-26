package nl.pim16aap2.cap.argument.specialized;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.parser.DoubleParser;

/**
 * Represents an argument that is parsed into a double.
 *
 * @author Pim
 */
public class DoubleArgument extends SpecializedArgument<Double>
{
    private static final @NonNull DoubleParser doubleParser = DoubleParser.create();

    public DoubleArgument()
    {
        super(doubleParser);
    }
}
