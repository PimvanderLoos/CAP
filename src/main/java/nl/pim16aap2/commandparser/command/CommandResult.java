package nl.pim16aap2.commandparser.command;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.commandsender.ICommandSender;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.CommandParserException;
import nl.pim16aap2.commandparser.exception.IllegalValueException;
import nl.pim16aap2.commandparser.renderer.IHelpCommandRenderer;
import nl.pim16aap2.commandparser.text.ColorScheme;
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
     * Sends the text rendered by {@link IHelpCommandRenderer#renderLongCommand(ColorScheme, Command)} to the {@link
     * #commandSender} for the {@link #command}.
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
     * See {@link IHelpCommandRenderer#renderFirstPage(ColorScheme, Command)}.
     */
    public void sendHelpMenu()
    {
        commandSender.sendMessage(command.getCommandManager().getHelpCommandRenderer()
                                         .renderFirstPage(commandSender.getColorScheme(), command));
    }

    /**
     * Sends the text rendered by {@link IHelpCommandRenderer#render(ColorScheme, Command, int)} (ColorScheme, Command)}
     * to the {@link #commandSender} for the {@link #command}. It will send the first page.
     */
    public void sendSubcommandHelp()
        throws IllegalValueException, CommandNotFoundException
    {
        command.getCommandManager().getHelpCommandRenderer().render(commandSender.getColorScheme(), command, null);
    }

    /**
     * Checks if help is required for the user. This is completely unrelated to the help command/argument, but rather to
     * incorrect usage of the current command (e.g. missing required argument(s)).
     *
     * @return True if the help message is to be sent to the user if {@link #run()} were to be called now.
     */
    public boolean helpRequired()
    {
        return this.parsedArguments == null;
//        if (this.parsedArguments == null)
//            return true;
//        final @NonNull Optional<Argument.IParsedArgument<Boolean>> helpArg = getParsedArgumentOpt("h");
//        return helpArg.isPresent() && helpArg.get().getValue() != null;
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
