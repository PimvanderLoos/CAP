package nl.pim16aap2.cap.argument.specialized;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.parser.IntegerParser;

/**
 * Represents an argument that is parsed into an integer.
 *
 * @author Pim
 */
public class IntegerArgument extends SpecializedArgument<Integer>
{
    private static final @NonNull IntegerParser integerParser = IntegerParser.create();

    public IntegerArgument()
    {
        super(integerParser);
    }
}
