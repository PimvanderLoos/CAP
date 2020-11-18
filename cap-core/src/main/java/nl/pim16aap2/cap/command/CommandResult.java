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

package nl.pim16aap2.cap.command;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CAPException;
import nl.pim16aap2.cap.exception.ExceptionHandler;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.renderer.IHelpCommandRenderer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents the result of parsing user input.
 *
 * @author Pim
 */
@Getter
public class CommandResult
{
    /**
     * The {@link Command} that was used. In case of multiple subcommands, this is the last command.
     * <p>
     * E.g. In the case of: '/mytopcommand subcommmand0 subcommand1', this would be 'subcommand1'.
     */
    private final @NonNull Command command;

    /**
     * The list of parsed {@link Argument}s.
     * <p>
     * The key for every entry is {@link Argument#getIdentifier()}.
     */
    private final @Nullable Map<@NonNull String, Argument.IParsedArgument<?>> parsedArguments;

    /**
     * The {@link ICommandSender} that issued the command. This object will be used for sending messages and permission
     * checking and its {@link ColorScheme} will be used to generate {@link Text} objects.
     */
    private final @NonNull ICommandSender commandSender;

    /**
     * @param commandSender   See {@link #commandSender}.
     * @param command         See {@link #command}.
     * @param parsedArguments See {@link #parsedArguments}.
     */
    public CommandResult(final @NonNull ICommandSender commandSender, final @NonNull Command command,
                         final @Nullable Map<@NonNull String, Argument.IParsedArgument<?>> parsedArguments)
    {
        this.commandSender = commandSender;
        this.command = command;
        this.parsedArguments = parsedArguments;
    }

    /**
     * Creates a new {@link CommandResult}, but without any {@link #parsedArguments}.
     *
     * @param commandSender See {@link #commandSender}.
     * @param command       See {@link #command}.
     */
    public CommandResult(final @NonNull ICommandSender commandSender, final @NonNull Command command)
    {
        this(commandSender, command, null);
    }

    /**
     * Sends the text rendered by {@link IHelpCommandRenderer#renderHelpMenu(ICommandSender, ColorScheme, Command)} to
     * the {@link #commandSender} for the {@link #command}.
     * <p>
     * See {@link Command#sendHelp(ICommandSender)}.
     */
    public void sendCommandHelp()
    {
        command.sendHelp(commandSender);
    }

    /**
     * Sends the first page of the help menu (with all the sub{@link Command}s) to the {@link #commandSender}.
     * <p>
     * See {@link IHelpCommandRenderer#renderFirstPage(ICommandSender, ColorScheme, Command)}.
     */
    public void sendHelpMenu()
    {
        commandSender.sendMessage(command.getCap().getHelpCommandRenderer()
                                         .renderFirstPage(commandSender, commandSender.getColorScheme(), command));
    }

    /**
     * Sends the text rendered by {@link IHelpCommandRenderer#renderOverviewPage(ICommandSender, ColorScheme, Command,
     * int)} (ColorScheme, Command)} to the {@link #commandSender} for the {@link #command}. It will send the first
     * page.
     */
    public void sendSubcommandHelp()
        throws ValidationFailureException
    {
        sendSubcommandHelp(1);
    }

    /**
     * Sends the text rendered by {@link IHelpCommandRenderer#renderOverviewPage(ICommandSender, ColorScheme, Command,
     * int)} (ColorScheme, Command)} to the {@link #commandSender} for the {@link #command}. It will send the first
     * page.
     *
     * @param page The number of the page to send. Note that counting starts at 1, not 0!
     */
    public void sendSubcommandHelp(final int page)
        throws ValidationFailureException
    {
        commandSender.sendMessage(command.getCap().getHelpCommandRenderer()
                                         .renderOverviewPage(commandSender, commandSender.getColorScheme(), command,
                                                             page));
    }

    /**
     * Checks if help is required for the user. This is completely unrelated to the help command/argument, but rather to
     * incorrect usage of the current command (e.g. missing required argument(s)).
     *
     * @return True if the help message is to be sent to the user if {@link #run()} were to be called now.
     */
    public boolean helpRequired()
    {
        return parsedArguments == null;
    }

    /**
     * Gets the parsed value associated with an {@link Argument}.
     *
     * @param identifier The label of the {@link Argument}. See {@link Argument#getIdentifier()}.
     * @param <T>        The type of the parsed value of the {@link Argument}.
     * @return The parsed value of the {@link Argument}.
     */
    @SuppressWarnings("unchecked")
    public <T> T getParsedArgument(final @NonNull String identifier)
    {
        // TODO: Handle this a bit better, maybe? This line shouldn't
        //       really be true if parsedArguments is null anyway.
        if (parsedArguments == null)
            return null;
        final Argument.IParsedArgument<T> result = (Argument.IParsedArgument<T>) parsedArguments.get(identifier);
        return result == null ? null : result.getValue();
    }

    /**
     * Executes {@link Command#commandExecutor}.
     */
    public void run()
    {
        try
        {
            if (helpRequired())
                command.sendHelp(commandSender);
            else
                command.getCommandExecutor().accept(this);
        }
        catch (final CAPException exception)
        {
            final @Nullable ExceptionHandler exceptionHandler = command.getCap().getExceptionHandler();
            if (exceptionHandler == null)
                throw new RuntimeException(exception);
            exceptionHandler.handleException(commandSender, exception);
        }
        catch (Throwable t)
        {
            if (command.getCap().getExceptionHandler() != null)
                command.getCap().getExceptionHandler().handleException(
                    commandSender,
                    new CAPException(
                        command.getCap().getLocalizer().getMessage("error.exception.generic", commandSender),
                        command.getCap().isDebug()));

            throw new RuntimeException("An error occurred trying to execute a command!\n" + toString(), t);
        }
    }

    @Override
    public String toString()
    {
        final @NonNull StringBuilder sb = new StringBuilder("CommandResult for command: \"")
            .append(command.getIdentifier()).append("\"").append("\n")
            .append("CommandSender: ").append(commandSender.toString()).append("\n");

        if (helpRequired())
            sb.append("Help required!");
        else if (parsedArguments == null)
            sb.append("Arguments:\nNULL");
        else
        {
            sb.append("Arguments:\n");

            for (@NonNull Map.Entry<@NonNull String, Argument.IParsedArgument<?>> argument : parsedArguments.entrySet())
                sb.append(argument.getKey()).append(": ").append(argument.getValue().toString()).append("\n");
        }

        return sb.toString();
    }
}
