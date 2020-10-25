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
        return render(colorScheme, argument, false);
    }

    @Override
    public @NonNull Text renderLongFormat(final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                          final @NonNull String summaryIndent)
    {
        final Text text = render(colorScheme, argument);
        if (argument.getLongName() != null)
            text.add(", ").add(render(colorScheme, argument, true));

        if (!argument.getSummary().equals(""))
            text.add("\n").add(summaryIndent).add(argument.getSummary(), TextType.SUMMARY);
        return text;
    }

    private @NonNull Text render(final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                 final boolean useLongName)
    {
        return argument.isRequired() ?
               renderRequired(colorScheme, argument, useLongName) :
               renderOptional(colorScheme, argument, useLongName);
    }

    protected @NonNull Text renderOptional(final @NonNull ColorScheme colorScheme,
                                           final @NonNull Argument<?> argument,
                                           final boolean useLongName)
    {
        final String suffix = argument.isRepeatable() ? "+" : "";
        return new Text(colorScheme).add("[", TextType.OPTIONAL_PARAMETER)
                                    .add(renderArgument(colorScheme, argument, useLongName))
                                    .add("]" + suffix, TextType.OPTIONAL_PARAMETER);
    }

    protected @NonNull Text renderRequired(final @NonNull ColorScheme colorScheme,
                                           final @NonNull Argument<?> argument,
                                           final boolean useLongName)
    {
        final String suffix = argument.isRepeatable() ? "+" : "";
        return new Text(colorScheme).add("<", TextType.REQUIRED_PARAMETER)
                                    .add(renderArgument(colorScheme, argument, useLongName))
                                    .add(">" + suffix, TextType.REQUIRED_PARAMETER);
    }

    protected @NonNull Text renderArgument(final @NonNull ColorScheme colorScheme,
                                           final @NonNull Argument<?> argument,
                                           final boolean useLongName)
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
            name = TextType.REQUIRED_PARAMETER_FLAG;
            label = TextType.REQUIRED_PARAMETER_LABEL;
            separator = TextType.REQUIRED_PARAMETER_SEPARATOR;
        }
        else
        {
            name = TextType.OPTIONAL_PARAMETER_FLAG;
            label = TextType.OPTIONAL_PARAMETER_LABEL;
            separator = TextType.OPTIONAL_PARAMETER_SEPARATOR;
        }

        // TODO: The '-' should be configurable, right?
        final @NonNull String argumentName = useLongName ?
                                             ("--" + argument.getLongName()) :
                                             ("-" + argument.getName());
        text.add(argumentName, name);

        if (argument.isValuesLess())
            return text;

        return text
            .add("=", separator) // FIXME: This should be configurable!
            .add(argLabel, label);
    }
}
