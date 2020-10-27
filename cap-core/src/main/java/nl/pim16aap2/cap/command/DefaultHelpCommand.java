package nl.pim16aap2.cap.command;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.StringArgument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.renderer.IHelpCommandRenderer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

@Getter
public class DefaultHelpCommand extends Command
{
    protected @NonNull IHelpCommandRenderer helpCommandRenderer;

    private static final List<Command> SUB_COMMANDS = Collections.emptyList();
    private static final Command HELP_COMMAND = null;
    private static final Argument<?> HELP_ARGUMENT = null;
    private static final boolean HIDDEN = false;
    private static final boolean ADD_DEFAULT_HELP_ARGUMENT = false;
    private static final boolean ADD_DEFAULT_HELP_SUB_COMMAND = false;
    private static final String PERMISSION = null;

    @Builder(builderMethodName = "helpCommandBuilder", toBuilder = true)
    public DefaultHelpCommand(final @Nullable String name, final @Nullable String description,
                              final @Nullable Function<ColorScheme, String> descriptionSupplier,
                              final @Nullable String summary,
                              final @Nullable Function<ColorScheme, String> summarySupplier,
                              final @Nullable String header,
                              final @Nullable Function<ColorScheme, String> headerSupplier,
                              final @NonNull CAP cap,
                              final @Nullable IHelpCommandRenderer helpCommandRenderer)
    {
        super(Util.valOrDefault(name, "help"), description, descriptionSupplier, summary, summarySupplier, header,
              headerSupplier, SUB_COMMANDS, HELP_COMMAND, DefaultHelpCommand::defaultHelpCommandExecutor,
              Collections.singletonList(new StringArgument().getOptionalPositional().name("page/command")
                                                            .summary("A page number of the name of a command.")
                                                            .longName("help").build()), HELP_ARGUMENT,
              HIDDEN, cap, ADD_DEFAULT_HELP_ARGUMENT, ADD_DEFAULT_HELP_SUB_COMMAND, PERMISSION);

        this.helpCommandRenderer = Util.valOrDefault(helpCommandRenderer, DefaultHelpCommandRenderer.getDefault());
    }

    public static @NonNull DefaultHelpCommand getDefault(final @NonNull CAP cap)
    {
        return DefaultHelpCommand.helpCommandBuilder()
                                 .cap(cap)
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

        final @NonNull Command command = superCommand.getCap().getCommand(val).orElse(superCommand);
        return helpCommandRenderer.render(commandSender, colorScheme, command, val);
    }


    protected static void defaultHelpCommandExecutor(final @NonNull CommandResult commandResult)
        throws IllegalValueException, CommandNotFoundException
    {
        if (!(commandResult.getCommand() instanceof DefaultHelpCommand))
            throw new CommandNotFoundException(commandResult.getCommand().getName(),
                                               commandResult.getCommand().getName() + " is not a help command!",
                                               commandResult.getCommand().getCap().isDebug());

        final @NonNull DefaultHelpCommand helpCommand = (DefaultHelpCommand) commandResult.getCommand();
        final @NonNull Command superCommand = helpCommand.getSuperCommand().orElseThrow(
            () -> new CommandNotFoundException("Super command of: " + helpCommand.getName(),
                                               commandResult.getCommand().getCap().isDebug()));

        final @NonNull ICommandSender commandSender = commandResult.getCommandSender();

        commandSender.sendMessage(renderHelpText(commandSender, commandSender.getColorScheme(), superCommand,
                                                 helpCommand.helpCommandRenderer,
                                                 commandResult.getParsedArgument("page/command")));
    }
}
