package nl.pim16aap2.commandparser.command;

import lombok.NonNull;
import nl.pim16aap2.commandparser.renderer.TextComponent;

public interface IHelpCommand
{
    /**
     * Renders the help text for a {@link Command}. This
     *
     * @param command The {@link Command} to get the help menu for.
     * @param page    The page number to display.
     * @return The {@link TextComponent} of the help message for the command.
     */
    @NonNull TextComponent render(final @NonNull Command command, final int page);


    @NonNull TextComponent render(final @NonNull Command command);
}
