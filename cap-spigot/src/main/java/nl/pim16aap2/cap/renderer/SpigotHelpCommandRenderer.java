package nl.pim16aap2.cap.renderer;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.DefaultHelpCommand;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextType;
import nl.pim16aap2.cap.text.decorator.ClickableTextCommandDecorator;
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
    protected void renderPageCountHeader(final @NonNull Text text, final int page, final int pageCount,
                                         @NonNull Command command)
    {
        @Nullable Command helpCommand = command.getHelpCommand();
        if (helpCommand == null)
        {
            // If there is no help command in sight, we cannot provide a link to the help command for this command.
            // When that's the case, just let the default renderer take care of the header.
            if (!(command instanceof DefaultHelpCommand))
            {
                super.renderPageCountHeader(text, page, pageCount, command);
                return;
            }

            // If the command is a help command, it has to have a supercommand (otherwise, whose help command is it?).
            // So, we'll have a command/helpcommand after all.
            helpCommand = command;
            command = command.getSuperCommand().orElseThrow(
                () -> new RuntimeException("Failed to find supercommand of help command!"));
        }

        final Text previousPage = new Text(text.getColorScheme());
        final Text nextPage = new Text(text.getColorScheme());

        if (page == 1)
            previousPage.add("---", TextType.REGULAR_TEXT);
        else
        {
            previousPage.add("<<<", TextType.COMMAND);
            previousPage.addDecorator(
                new ClickableTextCommandDecorator(0, previousPage.getLength(), String
                    .format("/%s %s %d", command.getName(), helpCommand.getName(), page - 1), "§cPrevious help page"));
        }

        if (page == pageCount)
            nextPage.add("---", TextType.REGULAR_TEXT);
        else
        {
            nextPage.add(">>>", TextType.COMMAND);
            nextPage.addDecorator(
                new ClickableTextCommandDecorator(0, nextPage.getLength(), String
                    .format("/%s %s %d", command.getName(), helpCommand.getName(), page + 1), "§cNext help page"));
        }

        text.add(previousPage)
            .add(String.format("---- Page (%2d / %2d) ---", page, pageCount), TextType.REGULAR_TEXT)
            .add(nextPage).add("\n");
    }

    @Override
    protected void renderCommand(final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                 final @NonNull Command command, final @NonNull String superCommands)
    {
        final @NonNull Command topCommand = command.getTopLevelCommand();
        final @Nullable Command helpCommand = topCommand.getHelpCommand();

        final int clickableStart = text.getLength();
        final String commandText = superCommands + command.getName();
        String clickableCommand = "";
        if (helpCommand != null)
        {
            final String commandName = command.getName().equals(helpCommand.getName()) ? "" : " " + command.getName();
            clickableCommand = "/" + topCommand.getName() + " " + helpCommand.getName() + commandName;
        }

        text.add(commandText, TextType.COMMAND);
        renderArgumentsShort(colorScheme, text, command);

        final int clickableEnd = text.getLength();
        text.addDecorator(new ClickableTextCommandDecorator(clickableStart, clickableEnd, clickableCommand,
                                                            "§cClick me for more information!"));

        if (!command.getSummary(colorScheme).equals(""))
            text.add("\n").add(descriptionIndent).add(command.getSummary(colorScheme), TextType.SUMMARY);
    }
}
