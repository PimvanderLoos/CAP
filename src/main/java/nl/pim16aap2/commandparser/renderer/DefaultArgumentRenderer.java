package nl.pim16aap2.commandparser.renderer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.text.ColorScheme;
import nl.pim16aap2.commandparser.text.Text;
import nl.pim16aap2.commandparser.text.TextType;

@RequiredArgsConstructor
public class DefaultArgumentRenderer implements IArgumentRenderer
{
    @Override
    public @NonNull Text render(final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument)
    {
        return argument.isRequired() ? renderRequired(colorScheme, argument) : renderOptional(colorScheme, argument);
    }

    @Override
    public @NonNull Text renderLong(final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                    final @NonNull String summaryIndent)
    {
        final Text text = render(colorScheme, argument);
        if (!argument.getSummary().equals(""))
            text.add("\n").add(summaryIndent).add(argument.getSummary(), TextType.SUMMARY);
        return text;
    }

    protected @NonNull Text renderOptional(final @NonNull ColorScheme colorScheme,
                                           final @NonNull Argument<?> argument)
    {
        final String suffix = argument.isRepeatable() ? "+" : "";
        return new Text(colorScheme).add("[", TextType.OPTIONAL_PARAMETER)
                                    .add(renderArgument(colorScheme, argument))
                                    .add("]" + suffix, TextType.OPTIONAL_PARAMETER);
    }

    protected @NonNull Text renderRequired(final @NonNull ColorScheme colorScheme,
                                           final @NonNull Argument<?> argument)
    {
        final String suffix = argument.isRepeatable() ? "+" : "";
        return new Text(colorScheme).add("<", TextType.REQUIRED_PARAMETER)
                                    .add(renderArgument(colorScheme, argument))
                                    .add(">" + suffix, TextType.REQUIRED_PARAMETER);
    }

    protected @NonNull Text renderArgument(final @NonNull ColorScheme colorScheme,
                                           final @NonNull Argument<?> argument)
    {
        final Text text = new Text(colorScheme);
        final String argLabel = argument.getLabel().equals("") ? argument.getName() : argument.getLabel();

        if (argument.isPositional())
            return text.add(argLabel, (argument.isRequired() ?
                                       TextType.REQUIRED_PARAMETER :
                                       TextType.OPTIONAL_PARAMETER_LABEL));

        final TextType name, label, separator;
        if (argument.isRequired())
        {
            // TODO: Make sure these types also exist for required params.
            name = TextType.OPTIONAL_PARAMETER_FLAG;
            label = TextType.OPTIONAL_PARAMETER_LABEL;
            separator = TextType.OPTIONAL_PARAMETER_SEPARATOR;
        }
        else
        {
            name = TextType.OPTIONAL_PARAMETER_FLAG;
            label = TextType.OPTIONAL_PARAMETER_LABEL;
            separator = TextType.OPTIONAL_PARAMETER_SEPARATOR;
        }

        // TODO: The '-' should be configurable, right?
        text.add("-" + argument.getName(), name);

        if (argument.isValuesLess())
            return text;

        return text
            .add("=", separator) // FIXME: This should be configurable!
            .add(argLabel, label);
    }
}
