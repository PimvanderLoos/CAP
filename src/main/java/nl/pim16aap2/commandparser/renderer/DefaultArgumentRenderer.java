package nl.pim16aap2.commandparser.renderer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.argument.OptionalArgument;
import nl.pim16aap2.commandparser.argument.RepeatableArgument;
import nl.pim16aap2.commandparser.argument.RequiredArgument;
import nl.pim16aap2.commandparser.text.ColorScheme;
import nl.pim16aap2.commandparser.text.Text;
import nl.pim16aap2.commandparser.text.TextType;

@RequiredArgsConstructor
public class DefaultArgumentRenderer implements IArgumentRenderer
{
    @Override
    public @NonNull Text render(final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument)
    {
        if (argument instanceof OptionalArgument)
            return renderOptional(colorScheme, (OptionalArgument<?>) argument);
        else if (argument instanceof RequiredArgument)
            return renderRequired(colorScheme, (RequiredArgument<?>) argument);
        // TODO: Ideally, this would go through the argument itself. Just store a function or something.
        //       Then apply the selected ArgumentRenderer (implements an interface).
        throw new RuntimeException("Failed to determine type of argument: " + argument.getClass().getCanonicalName());
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
                                           final @NonNull OptionalArgument<?> argument)
    {
        final String suffix = argument instanceof RepeatableArgument ? "+" : "";

        final Text rendered = new Text(colorScheme);
        rendered.add("[", TextType.OPTIONAL_PARAMETER)
                .add("-" + argument.getName(), TextType.OPTIONAL_PARAMETER_FLAG);

        if (!argument.getFlag())
        {
            rendered
                .add("=", TextType.OPTIONAL_PARAMETER_SEPARATOR) // FIXME: This should be configurable!
                .add(argument.getLabel(), TextType.OPTIONAL_PARAMETER_LABEL);
        }
        return rendered.add("]" + suffix, TextType.OPTIONAL_PARAMETER);
    }

    protected @NonNull Text renderRequired(final @NonNull ColorScheme colorScheme,
                                           final @NonNull RequiredArgument<?> argument)
    {
        return new Text(colorScheme).add("<" + argument.getLabel() + ">", TextType.REQUIRED_PARAMETER);
    }
}
