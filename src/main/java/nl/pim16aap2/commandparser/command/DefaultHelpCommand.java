package nl.pim16aap2.commandparser.command;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.commandsender.ICommandSender;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.IllegalValueException;
import nl.pim16aap2.commandparser.manager.CommandManager;
import nl.pim16aap2.commandparser.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.commandparser.renderer.IHelpCommandRenderer;
import nl.pim16aap2.commandparser.text.ColorScheme;
import nl.pim16aap2.commandparser.text.Text;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

@Getter
public class DefaultHelpCommand extends Command
{
    protected @NonNull IHelpCommandRenderer helpCommandRenderer;

    @Builder(builderMethodName = "helpCommandBuilder", toBuilder = true)
    public DefaultHelpCommand(final @Nullable String name, final @Nullable String description,
                              final @Nullable Function<ColorScheme, String> descriptionSupplier,
                              final @Nullable String summary,
                              final @Nullable Function<ColorScheme, String> summarySupplier,
                              final @Nullable String header,
                              final @Nullable Function<ColorScheme, String> headerSupplier,
                              final @NonNull CommandManager commandManager,
                              final @Nullable IHelpCommandRenderer helpCommandRenderer)
    {
        super(Util.valOrDefault(name, "help"), description, descriptionSupplier, summary, summarySupplier, header,
              headerSupplier, null, null, DefaultHelpCommand::defaultHelpCommandExecutor,
              Collections.singletonList(Argument.StringArgument.getOptionalPositional().name("page/command")
                                                               .summary("A page number of the name of a command.")
                                                               .longName("help").build()), null,
              false, commandManager, false, false, null);

        this.helpCommandRenderer = Util.valOrDefault(helpCommandRenderer, DefaultHelpCommandRenderer.getDefault());
    }

    public static @NonNull DefaultHelpCommand getDefault(final @NonNull CommandManager commandManager)
    {
        return DefaultHelpCommand.helpCommandBuilder()
                                 .commandManager(commandManager)
                                 .summary("Displays help information for this plugin and specific commands.")
                                 .header(
                                     "When no command or a page number is given, the usage help for the main command is displayed.\n" +
                                         "If a command is specified, the help for that command is shown.")
                                 .build();
    }

    @Override
    public @NonNull Optional<Command> getSubCommand(final @Nullable String name)
    {
        return Optional.empty();
    }

    protected static @NonNull Text renderHelpText(final @NonNull ICommandSender commandSender,
                                                  final @NonNull ColorScheme colorScheme,
                                                  final @NonNull Command superCommand,
                                                  final @NonNull IHelpCommandRenderer helpCommandRenderer,
                                                  final @Nullable String val)
        throws IllegalValueException, CommandNotFoundException
    {
        if (val == null)
            return helpCommandRenderer.render(commandSender, colorScheme, superCommand, null);

        final @NonNull OptionalInt intOpt = Util.parseInt(val);
        if (intOpt.isPresent())
            return helpCommandRenderer.render(commandSender, colorScheme, superCommand,
                                              intOpt.getAsInt() - helpCommandRenderer.getPageOffset());

        final @NonNull Command command = superCommand.getCommandManager().getCommand(val).orElse(superCommand);
        return helpCommandRenderer.render(commandSender, colorScheme, command, val);
    }


    protected static void defaultHelpCommandExecutor(final @NonNull CommandResult commandResult)
        throws IllegalValueException, CommandNotFoundException
    {
        if (!(commandResult.getCommand() instanceof DefaultHelpCommand))
            throw new CommandNotFoundException(commandResult.getCommand().getName(),
                                               commandResult.getCommand().getName() + " is not a help command!",
                                               commandResult.getCommand().getCommandManager().isDebug());

        final @NonNull DefaultHelpCommand helpCommand = (DefaultHelpCommand) commandResult.getCommand();
        final @NonNull Command superCommand = helpCommand.getSuperCommand().orElseThrow(
            () -> new CommandNotFoundException("Super command of: " + helpCommand.getName(),
                                               commandResult.getCommand().getCommandManager().isDebug()));

        final @NonNull ICommandSender commandSender = commandResult.getCommandSender();

        commandSender.sendMessage(renderHelpText(commandSender, commandSender.getColorScheme(), superCommand,
                                                 helpCommand.helpCommandRenderer,
                                                 commandResult.getParsedArgument("page/command")));
    }
}
