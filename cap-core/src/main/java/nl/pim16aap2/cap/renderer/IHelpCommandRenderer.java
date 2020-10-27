package nl.pim16aap2.cap.renderer;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import org.jetbrains.annotations.Nullable;

public interface IHelpCommandRenderer
{
    /**
     * Renders the help text for a {@link Command}.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link ICommandSender#hasPermission(Command)}.
     * @param colorScheme   The {@link ColorScheme} to use for rendering the help command.
     * @param command       The {@link Command} to get the help menu for.
     * @param page          The page number to display.
     * @return The {@link Text} of the help message for the command.
     */
    @NonNull Text render(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                         final @NonNull Command command, final int page)
        throws IllegalValueException;

    /**
     * Gets the page offset (caused by starting on a non-zero index, e.g. 1).
     *
     * @return The page offset.
     */
    int getPageOffset();

    /**
     * Either renders the help menu for a sub{@link Command} or a page of the help menu for the provided {@link
     * Command}, depending on the input value.
     * <p>
     * If the input value is an int, the page represented by that int is rendered for the provided {@link Command}.
     * <p>
     * If the input value is not an int, {@link #renderLongCommand(ICommandSender, ColorScheme, Command)} will be called
     * for the {@link Command} with the name of the value if one exists and is registered in the {@link CAP} of the
     * provided {@link Command}.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link ICommandSender#hasPermission(Command)}.
     * @param colorScheme   The {@link ColorScheme} to use for rendering the help text.
     * @param command       The {@link Command} for which to render a help page or whose {@link CAP} to use to look up
     *                      the (sub){@link Command} for which to print the long help menu.
     * @param val           The value representing either a help page (integer) or the name of a {@link Command}.
     * @return The rendered help menu.
     *
     * @throws IllegalValueException    If the provided value is an integer that is out of bounds for the provided
     *                                  number of help pages of the command.
     * @throws CommandNotFoundException If the provided value is not an integer and no {@link Command} with that String
     *                                  as name could be found.
     */
    @NonNull Text render(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                         final @NonNull Command command, final @Nullable String val)
        throws IllegalValueException, CommandNotFoundException;

    /**
     * Renders the long help menu for a {@link Command}.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link ICommandSender#hasPermission(Command)}.
     * @param colorScheme   The {@link ColorScheme} to use for rendering the help command.
     * @param command       The {@link Command} for which to render the long help menu.
     * @return The rendered long help menu for the given command.
     */
    @NonNull Text renderLongCommand(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                                    final @NonNull Command command);

    /**
     * Renders the first page of the help menu for the given {@link Command} with all its sub{@link Command}s.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link ICommandSender#hasPermission(Command)}.
     * @param colorScheme   The {@link ColorScheme} to use for rendering the help text.
     * @param command       The {@link Command} for which to render a help page or whose {@link CAP} to use to look up
     *                      the (sub){@link Command} for which to print the long help menu.
     * @return The rendered help menu.
     */
    @NonNull Text renderFirstPage(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                                  final @NonNull Command command);
}
