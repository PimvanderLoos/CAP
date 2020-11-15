package nl.pim16aap2.cap.command;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.Localization.ArgumentNamingSpec;
import nl.pim16aap2.cap.Localization.CommandNamingSpec;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.StringArgument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CAPException;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.renderer.IHelpCommandRenderer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.util.Functional.CheckedConsumer;
import nl.pim16aap2.cap.util.TabCompletionRequest;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;

/**
 * Represents the default help command.
 *
 * @author Pim
 */
@Getter
public class DefaultHelpCommand extends Command
{
    // These are the default values passed to the super constructor.
    private static final List<Command> SUB_COMMANDS = Collections.emptyList();
    private static final Command HELP_COMMAND = null;
    private static final Argument<?> HELP_ARGUMENT = null;
    private static final boolean VIRTUAL = false;
    private static final boolean ADD_DEFAULT_HELP_ARGUMENT = false;
    private static final boolean ADD_DEFAULT_HELP_SUB_COMMAND = false;
    private static final String PERMISSION = null;
    @Getter(AccessLevel.PRIVATE)
    private final boolean localized;

    /**
     * The default help argument. This is the non-localized version. If you want to enable localization, see {@link
     * #DEFAULT_HELP_ARGUMENT_LOCALIZED}.
     */
    public static final @NonNull Argument<@NonNull String> DEFAULT_HELP_ARGUMENT =
        new StringArgument()
            .getOptionalPositional()
            .nameSpec(ArgumentNamingSpec.RawStrings.builder().shortName("page/command")
                                                   .summary("A page number of the name of a command.").build())
            .identifier("helpArg")
            .tabCompleteFunction((DefaultHelpCommand::getSuggestions))
            .build();

    /**
     * The default help argument. This is the localized version. If you want to get a non-localized version, see {@link
     * #DEFAULT_HELP_ARGUMENT}.
     */
    public static final @NonNull Argument<@NonNull String> DEFAULT_HELP_ARGUMENT_LOCALIZED =
        new StringArgument()
            .getOptionalPositional()
            .nameSpec(new ArgumentNamingSpec.Localized("default.helpCommand.helpArgument"))
            .identifier("helpArg")
            .tabCompleteFunction((DefaultHelpCommand::getSuggestions))
            .build();

    public static final @NonNull CommandNamingSpec DEFAULT_COMMAND_NAMING_SPECIFICATION_LOCALIZED =
        new CommandNamingSpec.Localized("default.helpCommand");

    public static final @NonNull CommandNamingSpec DEFAULT_COMMAND_NAMING_SPECIFICATION_RAW =
        CommandNamingSpec.RawStrings
            .builder()
            .name("help")
            .summary("Displays help information for this plugin and specific commands.")
            .header("When no command or a page number is given, the usage help for the main command is displayed.\n" +
                        "  If a command is specified, the help for that command is shown.")
            .build();

    /**
     * @param descriptionSupplier The supplier that is used to build the description. Note that this isn't used in case
     *                            the description is provided.
     * @param summarySupplier     The supplier that is used to build the summary. Note that this isn't used in case a
     *                            summary is provided.
     * @param headerSupplier      The supplier that is used to build the header. Note that this isn't used in case a
     *                            header is provided.
     * @param commandExecutor     The function that will be executed by {@link CommandResult#run()}.
     * @param cap                 The {@link CAP} instance that manages this command.
     * @param localized           Whether or not to use the default localized naming specifications and arguments (if
     *                            none are manually specified).
     */
    protected DefaultHelpCommand(final @Nullable CommandNamingSpec commandNamingSpec,
                                 final @Nullable Function<ICommandSender, String> descriptionSupplier,
                                 final @Nullable Function<ICommandSender, String> summarySupplier,
                                 final @Nullable Function<ICommandSender, String> headerSupplier,
                                 final @Nullable Argument<?> helpArgument,
                                 final @NonNull CheckedConsumer<@NonNull CommandResult, CAPException> commandExecutor,
                                 final @NonNull CAP cap, final boolean localized)
    {
        super(Util.valOrDefault(commandNamingSpec, localized ?
                                                   DEFAULT_COMMAND_NAMING_SPECIFICATION_LOCALIZED :
                                                   DEFAULT_COMMAND_NAMING_SPECIFICATION_RAW),
              descriptionSupplier, summarySupplier, headerSupplier, SUB_COMMANDS, HELP_COMMAND,
              ADD_DEFAULT_HELP_ARGUMENT, HELP_ARGUMENT, ADD_DEFAULT_HELP_SUB_COMMAND, commandExecutor,
              Collections.singletonList(Util.valOrDefault(helpArgument, DEFAULT_HELP_ARGUMENT)), VIRTUAL, cap,
              ((commandSender, command) -> true));
        this.localized = localized;
    }

    /**
     * @param nameSpec            See {@link Command#nameSpec}.
     * @param descriptionSupplier The supplier that is used to build the description. Note that this isn't used in case
     *                            the description is provided.
     * @param summarySupplier     The supplier that is used to build the summary. Note that this isn't used in case a
     *                            summary is provided.
     * @param headerSupplier      The supplier that is used to build the header. Note that this isn't used in case a
     *                            header is provided.
     * @param cap                 The {@link CAP} instance that manages this command.
     * @param localized           Whether or not to use the default localized naming specifications and arguments (if
     *                            none are manually specified).
     */
    @Builder(builderMethodName = "helpCommandBuilder", toBuilder = true)
    public DefaultHelpCommand(final @Nullable CommandNamingSpec nameSpec,
                              final @Nullable Function<ICommandSender, String> descriptionSupplier,
                              final @Nullable Function<ICommandSender, String> summarySupplier,
                              final @Nullable Function<ICommandSender, String> headerSupplier,
                              final @Nullable Argument<?> helpArgument,
                              final @NonNull CAP cap,
                              final @Nullable Boolean localized)
    {
        this(nameSpec, descriptionSupplier, summarySupplier, headerSupplier, helpArgument,
             DefaultHelpCommand::defaultHelpCommandExecutor, cap, Util.valOrDefault(localized, true));
    }

    /**
     * Gets all the potential tab-completion suggestions.
     *
     * @param request The {@link TabCompletionRequest} containing the data required to build a list of suggestions.
     * @return The list of potential tab-completion suggestions.
     */
    public static @NonNull List<@NonNull String> getSuggestions(final @NonNull TabCompletionRequest request)
    {
        // TODO: Add support for numerical stuff using the pageCount stuff from the renderer.
        final @NonNull Command command = request.getCommand();
        if (!(command instanceof DefaultHelpCommand) || !command.getSuperCommand().isPresent() ||
            command.getSuperCommand().get().getSubCommandCount() == 0)
            return new ArrayList<>(0);

        final @NonNull Command superCommand = command.getSuperCommand().get();
        return new ArrayList<>(superCommand.getSubCommandNames(request.getCommandSender().getLocale()));
    }

    /**
     * Gets a new {@link DefaultHelpCommand} with all the default settings. Use {@link #toBuilder()} if you wish to
     * modify these settings (or just create a new one).
     *
     * @param cap The {@link CAP} that manages this command.
     * @return A new {@link DefaultHelpCommand}.
     */
    public static @NonNull DefaultHelpCommand getDefault(final @NonNull CAP cap)
    {
        return getDefault(cap, false);
    }

    /**
     * Gets a new {@link DefaultHelpCommand} with all the default settings. Use {@link #toBuilder()} if you wish to
     * modify these settings (or just create a new one).
     *
     * @param cap                The {@link CAP} that manages this command.
     * @param enableLocalization Set to true to use localized names for this help command and its arguments. When set to
     *                           true, the name will be 'default.helpCommand.name', when false it will be 'help'.
     *                           <p>
     *                           For the command name, this doesn't apply if the name was explicitly specified.
     * @return A new {@link DefaultHelpCommand}.
     */
    public static @NonNull DefaultHelpCommand getDefault(final @NonNull CAP cap, final boolean enableLocalization)
    {
        return DefaultHelpCommand
            .helpCommandBuilder().cap(cap).localized(enableLocalization)
            .build();
    }

    protected static @NonNull Text renderHelpText(final @NonNull ICommandSender commandSender,
                                                  final @NonNull ColorScheme colorScheme,
                                                  final @NonNull Command superCommand,
                                                  final @NonNull IHelpCommandRenderer helpCommandRenderer,
                                                  final @Nullable String val)
        throws ValidationFailureException, CommandNotFoundException
    {
        // TODO: Isn't this already handled by the DefaultHelpCommandRenderer?
        if (val == null)
            return helpCommandRenderer.render(commandSender, colorScheme, superCommand, null);

        final @NonNull OptionalInt intOpt = Util.parseInt(val);
        if (intOpt.isPresent())
            return helpCommandRenderer.renderOverviewPage(commandSender, colorScheme, superCommand, intOpt.getAsInt());

        final @NonNull Command command = superCommand.getCap().getCommand(val, commandSender.getLocale())
                                                     .orElse(superCommand);
        return helpCommandRenderer.render(commandSender, colorScheme, command, val);
    }

    /**
     * Executes the default action: Print the help menu and send it to the {@link ICommandSender}.
     *
     * @param commandResult The {@link CommandResult} to base the help menu on.
     * @throws ValidationFailureException If the provided value for the help message is invalid. E.g. '/command help 10'
     *                                    if there are only 8 pages available.
     * @throws CommandNotFoundException   If the specified command could not be found. E.g. '/command help mySubCommand'
     *                                    if the command called 'mySubCommand' does not exist or is not registered in
     *                                    the same {@link CAP}.
     * @see ICommandSender#sendMessage(Text)
     */
    protected static void defaultHelpCommandExecutor(final @NonNull CommandResult commandResult)
        throws ValidationFailureException, CommandNotFoundException
    {
        if (!(commandResult.getCommand() instanceof DefaultHelpCommand))
            throw new IllegalArgumentException("Command " + commandResult.getCommand().getIdentifier() +
                                                   " is not a help command!");

        final @NonNull DefaultHelpCommand helpCommand = (DefaultHelpCommand) commandResult.getCommand();
        final @NonNull Command superCommand = helpCommand.getSuperCommand().orElseThrow(
            () -> new IllegalStateException(
                "HelpCommand " + helpCommand.getIdentifier() + " does not have a super command!"));

        final @NonNull ICommandSender commandSender = commandResult.getCommandSender();

        commandSender.sendMessage(renderHelpText(commandSender, commandSender.getColorScheme(), superCommand,
                                                 helpCommand.getCap().getHelpCommandRenderer(),
                                                 commandResult.getParsedArgument("helpArg")));
    }
}
