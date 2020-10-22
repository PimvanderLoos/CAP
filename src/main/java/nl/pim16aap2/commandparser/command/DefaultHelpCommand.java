package nl.pim16aap2.commandparser.command;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
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

@Getter
public class DefaultHelpCommand extends Command
{
    protected @NonNull IHelpCommandRenderer helpCommandRenderer;

    @Builder(builderMethodName = "helpCommandBuilder")
    public DefaultHelpCommand(final @Nullable String name, final @Nullable String description,
                              final @Nullable String summary, final @Nullable String header,
                              final @NonNull CommandManager commandManager,
                              final @Nullable IHelpCommandRenderer helpCommandRenderer)
    {
        super(Util.valOrDefault(name, "help"), description, summary, null,
              DefaultHelpCommand::defaultHelpCommandExecutor,
              Collections.singletonList(Argument.StringArgument.getOptional().name("h").label("help")
                                                               .summary("A page number of the name of a command.")
                                                               .longName("help").build()),
              false, header, commandManager, false, false);

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

    protected static void defaultHelpCommandExecutor(final @NonNull CommandResult commandResult)
        throws IllegalValueException, CommandNotFoundException
    {
        final @NonNull CommandManager commandManager = commandResult.getCommand().commandManager;
        if (!(commandResult.getCommand() instanceof DefaultHelpCommand))
            throw new CommandNotFoundException(commandResult.getCommand().getName() + " is not a help command!");

        final @NonNull DefaultHelpCommand helpCommand = (DefaultHelpCommand) commandResult.getCommand();
        final @NonNull Command superCommand = helpCommand.getSuperCommand().orElseThrow(
            () -> new CommandNotFoundException("Super command of: " + helpCommand.getName()));

        final @NonNull ColorScheme colorScheme = commandManager.getColorScheme();

        final Text help = helpCommand.helpCommandRenderer
            .render(colorScheme, superCommand, commandResult.getParsedArgument("h"));

        commandManager.getTextConsumer().accept(help);
    }
}
