/*
 *  MIT License
 *
 * Copyright (c) 2020 Pim van der Loos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nl.pim16aap2.cap.renderer;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import org.jetbrains.annotations.Nullable;

public interface IHelpCommandRenderer
{
    /**
     * Renders the help overview page for a {@link Command}.
     * <p>
     * This is the menu showing all a command's subcommands.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link Command#hasPermission(ICommandSender)}.
     * @param colorScheme   The {@link ColorScheme} to use for rendering the help command.
     * @param command       The {@link Command} to get the help menu for.
     * @param page          The page number to display.
     * @return The {@link Text} of the help message for the command.
     */
    @NonNull Text renderOverviewPage(final @NonNull ICommandSender commandSender,
                                     final @NonNull ColorScheme colorScheme,
                                     final @NonNull Command command, final int page)
        throws ValidationFailureException;

    /**
     * Either renders the help menu for a sub{@link Command} or a page of the help menu for the provided {@link
     * Command}, depending on the input value.
     * <p>
     * If the input value is an int, the page represented by that int is rendered for the provided {@link Command}.
     * <p>
     * If the input value is not an int, {@link #renderHelpMenu(ICommandSender, ColorScheme, Command)} will be called
     * for the {@link Command} with the name of the value if one exists and is registered in the {@link CAP} of the
     * provided {@link Command}.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link Command#hasPermission(ICommandSender)}.
     * @param colorScheme   The {@link ColorScheme} to use for rendering the help text.
     * @param command       The {@link Command} for which to render a help page or whose {@link CAP} to use to look up
     *                      the (sub){@link Command} for which to print the long help menu.
     * @param val           The value representing either a help page (integer) or the name of a {@link Command}.
     * @return The rendered help menu.
     *
     * @throws ValidationFailureException If the provided value is an integer that is out of bounds for the provided
     *                                    number of help pages of the command.
     * @throws CommandNotFoundException   If the provided value is not an integer and no {@link Command} with that
     *                                    String as name could be found.
     */
    @NonNull Text render(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                         final @NonNull Command command, final @Nullable String val)
        throws ValidationFailureException, CommandNotFoundException;

    /**
     * Renders the long help menu for a {@link Command}.
     * <p>
     * This shows all the arguments and their explanations for this command.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link Command#hasPermission(ICommandSender)}.
     * @param colorScheme   The {@link ColorScheme} to use for rendering the help command.
     * @param command       The {@link Command} for which to render the long help menu.
     * @return The rendered long help menu for the given command.
     */
    @NonNull Text renderHelpMenu(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                                 final @NonNull Command command);

    /**
     * Renders the first page of the help overview for the given {@link Command} with all its sub{@link Command}s.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link Command#hasPermission(ICommandSender)}.
     * @param colorScheme   The {@link ColorScheme} to use for rendering the help text.
     * @param command       The {@link Command} for which to render a help page or whose {@link CAP} to use to look up
     *                      the (sub){@link Command} for which to print the long help menu.
     * @return The rendered help menu.
     */
    @NonNull Text renderFirstPage(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                                  final @NonNull Command command);
}
