package nl.pim16aap2.cap.argument.specialized;


import lombok.NonNull;
import nl.pim16aap2.cap.argument.parser.StringParser;

/**
 * Represents an argument that is parsed into a String.
 *
 * @author Pim
 */
public class StringArgument extends SpecializedArgument<String>
{
    private static final @NonNull StringParser stringParser = StringParser.create();

    public StringArgument()
    {
        super(stringParser);
    }
}
