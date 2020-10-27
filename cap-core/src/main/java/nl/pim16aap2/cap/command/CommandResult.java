package nl.pim16aap2.cap.command;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.CommandParserException;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.renderer.IHelpCommandRenderer;
import nl.pim16aap2.cap.text.ColorScheme;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

@Getter
public class CommandResult
{
    private final @NonNull Command command;
    private final @Nullable Map<@NonNull String, Argument.IParsedArgument<?>> parsedArguments;
    private final @NonNull ICommandSender commandSender;

    public CommandResult(final @NonNull ICommandSender commandSender, final @NonNull Command command,
                         final @Nullable Map<@NonNull String, Argument.IParsedArgument<?>> parsedArguments)
    {
        this.commandSender = commandSender;
        this.command = command;
        this.parsedArguments = parsedArguments;
    }

    public CommandResult(final @NonNull ICommandSender commandSender, final @NonNull Command command)
    {
        this(commandSender, command, null);
    }

    /**
     * Sends the text rendered by {@link IHelpCommandRenderer#renderLongCommand(ICommandSender, ColorScheme, Command)}
     * to the {@link #commandSender} for the {@link #command}.
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
     * Sends the text rendered by {@link IHelpCommandRenderer#render(ICommandSender, ColorScheme, Command, int)}
     * (ColorScheme, Command)} to the {@link #commandSender} for the {@link #command}. It will send the first page.
     */
    public void sendSubcommandHelp()
        throws IllegalValueException, CommandNotFoundException
    {
        command.getCap().getHelpCommandRenderer()
               .render(commandSender, commandSender.getColorScheme(), command, null);
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

    @SuppressWarnings("unchecked")
    public @NonNull <T> Optional<Argument.IParsedArgument<T>> getParsedArgumentOpt(final @NonNull String name)
    {
        if (parsedArguments == null)
            return Optional.empty();
        return Optional.ofNullable((Argument.IParsedArgument<T>) parsedArguments.get(name));
    }

    @SuppressWarnings("unchecked")
    public <T> T getParsedArgument(final @NonNull String name)
    {
        // TODO: Handle this a bit better, maybe? This line shouldn't
        //       really be true if parsedArguments is null anyway.
        if (parsedArguments == null)
            return null;
        final Argument.IParsedArgument<T> result = (Argument.IParsedArgument<T>) parsedArguments.get(name);
        return result == null ? null : result.getValue();
    }

    public void run()
        throws CommandParserException
    {
        if (helpRequired())
            command.sendHelp(commandSender);
        else
            command.getCommandExecutor().accept(this);
    }
}
