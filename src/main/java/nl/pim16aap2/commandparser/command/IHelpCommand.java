package nl.pim16aap2.commandparser.command;

import lombok.NonNull;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.IllegalValueException;
import nl.pim16aap2.commandparser.manager.CommandManager;
import nl.pim16aap2.commandparser.renderer.Text;

public interface IHelpCommand
{
    /**
     * Renders the help text for a {@link Command}. This
     *
     * @param command The {@link Command} to get the help menu for.
     * @param page    The page number to display.
     * @return The {@link Text} of the help message for the command.
     */
    @NonNull Text render(final @NonNull Command command, final int page)
        throws IllegalValueException;

    /**
     * Either renders the help menu for a sub{@link Command} or a page of the help menu for the provided {@link
     * Command}, depending on the input value.
     * <p>
     * If the input value is an int, the page represented by that int is rendered for the provided {@link Command}.
     * <p>
     * If the input value is not an int, {@link #renderLongCommand(Command)} will be called for the {@link Command} with
     * the name of the value if one exists and is registered in the {@link CommandManager} of the provided {@link
     * Command}.
     *
     * @param command The {@link Command} for which to render a help page or whose {@link CommandManager} to use to look
     *                up the (sub){@link Command} for which to print the long help menu.
     * @param val     The value representing either a help page (integer) or the name of a {@link Command}/
     * @return The rendered help menu.
     *
     * @throws IllegalValueException    If the provided value is an integer that is out of bounds for the provided
     *                                  number of help pages of the command.
     * @throws CommandNotFoundException If the provided value is not an integer and no {@link Command} with that String
     *                                  as name could be found.
     */
    // TODO: IllegalValueException is only used for OOB page values, so maybe rename it to something more specific to that?
    @NonNull Text render(final @NonNull Command command, final @NonNull String val)
        throws IllegalValueException, CommandNotFoundException;

    /**
     * Renders the long help menu for a {@link Command}.
     *
     * @param command The {@link Command} for which to render the long help menu.
     * @return The rendered long help menu for the given command.
     */
    @NonNull Text renderLongCommand(final @NonNull Command command);
}
