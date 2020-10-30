package nl.pim16aap2.cap.renderer;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextType;
import nl.pim16aap2.cap.text.decorator.ClickableTextDecorator;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the default implementation of {@link IHelpCommandRenderer}.
 *
 * @author Pim
 */
@SuperBuilder(toBuilder = true)
@Getter
public class SpigotHelpCommandRenderer extends DefaultHelpCommandRenderer
{
    @Builder.Default
    @NonNull String hoverMessage = "Click to see the help menu for this command!";

    /**
     * Gets a new instance of this {@link SpigotHelpCommandRenderer} using the default values.
     * <p>
     * Use {@link SpigotHelpCommandRenderer#toBuilder()} if you wish to customize it.
     *
     * @return A new instance of this {@link SpigotHelpCommandRenderer}.
     */
    public static @NonNull SpigotHelpCommandRenderer getDefault()
    {
        return SpigotHelpCommandRenderer.builder().build();
    }

    @Override
    protected void renderPageCountHeader(final @NonNull Text text, final int page, final int pageCount)
    {
        text.add(String.format("------- Page (%2d / %2d) -------\n",
                               page + getPageOffset(), pageCount + getPageOffset()));
    }

    @Override
    protected void renderCommand(final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                 final @NonNull Command command, final @NonNull String superCommands)
    {
        final @NonNull Command topCommand = getTopLevelCommand(command);
        final @Nullable Command helpCommand = topCommand.getHelpCommand();

        final int clickableStart = text.getLength();
        String commandText = superCommands + command.getName();
        String clickableCommand = "";
        if (helpCommand != null)
        {
            String commandName = command.getName().equals(helpCommand.getName()) ? "" : " " + command.getName();
            clickableCommand = "/" + topCommand.getName() + " " + helpCommand.getName() + commandName;
        }

        text.add(commandText, TextType.COMMAND);
        renderArgumentsShort(colorScheme, text, command);

        final int clickableEnd = text.getLength();
        text.addDecorator(
            new ClickableTextDecorator(clickableStart, clickableEnd, clickableCommand, "§cHOVER MESSAGE"));

        if (!command.getSummary(colorScheme).equals(""))
            text.add("\n").add(descriptionIndent).add(command.getSummary(colorScheme), TextType.SUMMARY);
    }

    private static @NonNull Command getTopLevelCommand(final @NonNull Command command)
    {
        return command.getSuperCommand().map(SpigotHelpCommandRenderer::getTopLevelCommand).orElse(command);
    }
}
