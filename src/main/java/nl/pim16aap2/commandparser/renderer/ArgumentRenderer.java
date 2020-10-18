package nl.pim16aap2.commandparser.renderer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.argument.OptionalArgument;
import nl.pim16aap2.commandparser.argument.RepeatableArgument;
import nl.pim16aap2.commandparser.argument.RequiredArgument;

@RequiredArgsConstructor
public class ArgumentRenderer
{
    protected final @NonNull ColorScheme colorScheme;

    public @NonNull TextComponent render(final @NonNull Argument<?> argument)
    {
        if (argument instanceof OptionalArgument)
            return renderOptional((OptionalArgument<?>) argument);
        else if (argument instanceof RequiredArgument)
            return renderRequired((RequiredArgument<?>) argument);
        // TODO: Ideally, this would go through the argument itself. Just store a function or something.
        //       Then apply the selected ArgumentRenderer (implements an interface).
        throw new RuntimeException("Failed to determine type of argument: " + argument.getClass().getCanonicalName());
    }

    public @NonNull TextComponent renderLong(final @NonNull Argument<?> argument, final @NonNull String summaryIndent)
    {
        return render(argument).add("\n").add(summaryIndent).add(argument.getSummary(), TextType.SUMMARY);
    }

    protected @NonNull TextComponent renderOptional(final @NonNull OptionalArgument<?> argument)
    {
        final String suffix = argument instanceof RepeatableArgument ? "+" : "";

        final TextComponent rendered = new TextComponent(colorScheme);
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

    protected @NonNull TextComponent renderRequired(final @NonNull RequiredArgument<?> argument)
    {
        return new TextComponent(colorScheme).add("<" + argument.getLabel() + ">", TextType.REQUIRED_PARAMETER);
    }
}
