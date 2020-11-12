package nl.pim16aap2.cap.command;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.StringArgument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CAPException;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.IllegalValueException;
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
import java.util.Locale;
import java.util.Optional;
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
    protected @NonNull IHelpCommandRenderer helpCommandRenderer;

    // These are the default values passed to the super constructor.
    private static final List<Command> SUB_COMMANDS = Collections.emptyList();
    private static final Command HELP_COMMAND = null;
    private static final Argument<?> HELP_ARGUMENT = null;
    private static final boolean VIRTUAL = false;
    private static final boolean ADD_DEFAULT_HELP_ARGUMENT = false;
    private static final boolean ADD_DEFAULT_HELP_SUB_COMMAND = false;
    private static final String PERMISSION = null;

    /**
     * The number of subcommands the supercommand had the last time we checked. This is used to invalidate {@link
     * #suggestions} when any subcommands are added, so that they are always up-to-date.
     */
    @Getter(AccessLevel.PRIVATE)
    private int lastSubCommandCount = 0;

    /**
     * The list of tab-completion suggestions.
     */
    @Getter(AccessLevel.PRIVATE)
    private List<String> suggestions = null;

    @Getter(AccessLevel.PRIVATE)
    private final boolean enableLocalization;

    /**
     * The default help argument. This is the non-localized version. If you want to enable localization, see {@link
     * #DEFAULT_HELP_ARGUMENT_LOCALIZED}.
     */
    public static final @NonNull Argument<@NonNull String> DEFAULT_HELP_ARGUMENT =
        new StringArgument()
            .getOptionalPositional()
            .shortName("page/command")
            .summary("A page number of the name of a command.")
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
            .shortName("default.helpCommand.helpArgument.shortName")
            .summary("default.helpCommand.helpArgument.summary")
            .identifier("helpArg")
            .tabCompleteFunction((DefaultHelpCommand::getSuggestions))
            .build();

    /**
     * @param name                The name of the command.
     * @param description         The description of the command. This is the longer description shown in the help menu
     *                            for this command.
     * @param descriptionSupplier The supplier that is used to build the description. Note that this isn't used in case
     *                            the description is provided.
     * @param summary             The summary of the command. This is the short description shown in the list of
     *                            commands.
     * @param summarySupplier     The supplier that is used to build the summary. Note that this isn't used in case a
     *                            summary is provided.
     * @param header              The header of the command. This is text shown at the top of the help menu for this
     *                            command.
     * @param headerSupplier      The supplier that is used to build the header. Note that this isn't used in case a
     *                            header is provided.
     * @param commandExecutor     The function that will be executed by {@link CommandResult#run()}.
     * @param cap                 The {@link CAP} instance that manages this command.
     * @param enableLocalization  Set to true to use localized names for this help command and its arguments. When set
     *                            to true, the name will be 'default.helpCommand.name', when false it will be 'help'.
     *                            <p>
     *                            For the command name, this doesn't apply if the name was explicitly specified.
     */
    protected DefaultHelpCommand(final @Nullable String name, final @Nullable String description,
                                 final @Nullable Function<ICommandSender, String> descriptionSupplier,
                                 final @Nullable String summary,
                                 final @Nullable Function<ICommandSender, String> summarySupplier,
                                 final @Nullable String header,
                                 final @Nullable Function<ICommandSender, String> headerSupplier,
                                 final @Nullable String sectionTitle,
                                 final @Nullable Argument<?> helpArgument,
                                 final @NonNull CheckedConsumer<@NonNull CommandResult, CAPException> commandExecutor,
                                 final @NonNull CAP cap,
                                 final @Nullable IHelpCommandRenderer helpCommandRenderer,
                                 final boolean enableLocalization)
    {
        super(Util.valOrDefault(name, enableLocalization ? "default.helpCommand.name" : "help"), description,
              descriptionSupplier, summary, summarySupplier, header,
              headerSupplier, sectionTitle, SUB_COMMANDS, HELP_COMMAND, ADD_DEFAULT_HELP_ARGUMENT, HELP_ARGUMENT,
              ADD_DEFAULT_HELP_SUB_COMMAND, commandExecutor,
              Collections.singletonList(Util.valOrDefault(helpArgument,
                                                          enableLocalization ? DEFAULT_HELP_ARGUMENT_LOCALIZED :
                                                          DEFAULT_HELP_ARGUMENT)), VIRTUAL, cap,
              ((commandSender, command) -> true));

        this.helpCommandRenderer = Util.valOrDefault(helpCommandRenderer, cap.getHelpCommandRenderer());
        this.enableLocalization = enableLocalization;
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

        final @NonNull DefaultHelpCommand helpCommand = (DefaultHelpCommand) command;
        final @NonNull Command superCommand = command.getSuperCommand().get();

        if (helpCommand.suggestions == null || helpCommand.getSubCommandCount() != helpCommand.lastSubCommandCount)
        {
            helpCommand.suggestions = new ArrayList<>(helpCommand.getSubCommandCount());
            addAllSubCommands(request.getCommandSender().getLocale(), helpCommand.suggestions,
                              superCommand, helpCommand);
            helpCommand.suggestions = Collections.unmodifiableList(helpCommand.suggestions);
        }

        return helpCommand.suggestions;
    }

    /**
     * Recursively adds all sub{@link Command}s of a {@link Command} to a target list.
     *
     * @param locale       The {@link Locale} to use for the name localization.
     * @param target       The target list to add all sub{@link Command}s to.
     * @param superCommand The super{@link Command} whose sub{@link Command}s to add to the target list.
     * @param helpCommand  The {@link DefaultHelpCommand} that will be ignored for the target list.
     */
    private static void addAllSubCommands(final @Nullable Locale locale, final @NonNull List<String> target,
                                          final @NonNull Command superCommand,
                                          final @NonNull DefaultHelpCommand helpCommand)
    {
        if (superCommand.getName(locale).equals(helpCommand.getName(locale)))
            return;
        target.add(superCommand.getName(locale));
        superCommand.getSubCommands().forEach(subCommand -> addAllSubCommands(locale, target, subCommand, helpCommand));
    }

    /**
     * @param name                The name of the command.
     * @param description         The description of the command. This is the longer description shown in the help menu
     *                            for this command.
     * @param descriptionSupplier The supplier that is used to build the description. Note that this isn't used in case
     *                            the description is provided.
     * @param summary             The summary of the command. This is the short description shown in the list of
     *                            commands.
     * @param summarySupplier     The supplier that is used to build the summary. Note that this isn't used in case a
     *                            summary is provided.
     * @param header              The header of the command. This is text shown at the top of the help menu for this
     *                            command.
     * @param headerSupplier      The supplier that is used to build the header. Note that this isn't used in case a
     *                            header is provided.
     * @param cap                 The {@link CAP} instance that manages this command.
     * @param enableLocalization  Set to true to use localized names for this help command and its arguments. When set
     *                            to true, the name will be 'default.helpCommand.name', when false it will be 'help'.
     *                            <p>
     *                            For the command name, this doesn't apply if the name was explicitly specified.
     */
    @Builder(builderMethodName = "helpCommandBuilder", toBuilder = true)
    public DefaultHelpCommand(final @Nullable String name, final @Nullable String description,
                              final @Nullable Function<ICommandSender, String> descriptionSupplier,
                              final @Nullable String summary,
                              final @Nullable Function<ICommandSender, String> summarySupplier,
                              final @Nullable String header,
                              final @Nullable Function<ICommandSender, String> headerSupplier,
                              final @Nullable String sectionTitle,
                              final @Nullable Argument<?> helpArgument,
                              final @NonNull CAP cap,
                              final @Nullable IHelpCommandRenderer helpCommandRenderer,
                              final boolean enableLocalization)
    {
        this(name, description, descriptionSupplier, summary, summarySupplier, header, headerSupplier, sectionTitle,
             helpArgument, DefaultHelpCommand::defaultHelpCommandExecutor, cap, helpCommandRenderer,
             enableLocalization);
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
            .helpCommandBuilder().cap(cap)
            .enableLocalization(enableLocalization)
            .summary("default.helpCommand.summary")
            .header("default.helpCommand.header")
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
        // TODO: Isn't this already handled by the DefaultHelpCommandRenderer?
        if (val == null)
            return helpCommandRenderer.render(commandSender, colorScheme, superCommand, null);

        final @NonNull OptionalInt intOpt = Util.parseInt(val);
        if (intOpt.isPresent())
            return helpCommandRenderer.renderOverviewPage(commandSender, colorScheme, superCommand, intOpt.getAsInt());

        final @NonNull Command command = superCommand.getCap().getCommand(val).orElse(superCommand);
        return helpCommandRenderer.render(commandSender, colorScheme, command, val);
    }

    /**
     * Executes the default action: Print the help menu and send it to the {@link ICommandSender}.
     *
     * @param commandResult The {@link CommandResult} to base the help menu on.
     * @throws IllegalValueException    If the provided value for the help message is invalid. E.g. '/command help 10'
     *                                  if there are only 8 pages available.
     * @throws CommandNotFoundException If the specified command could not be found. E.g. '/command help mySubCommand'
     *                                  if the command called 'mySubCommand' does not exist or is not registered in the
     *                                  same {@link CAP}.
     * @see ICommandSender#sendMessage(Text)
     */
    protected static void defaultHelpCommandExecutor(final @NonNull CommandResult commandResult)
        throws IllegalValueException, CommandNotFoundException
    {
        if (!(commandResult.getCommand() instanceof DefaultHelpCommand))
            throw new IllegalArgumentException("Command " + commandResult.getCommand().getNameKey() +
                                                   " is not a help command!");

        final @NonNull DefaultHelpCommand helpCommand = (DefaultHelpCommand) commandResult.getCommand();
        final @NonNull Command superCommand = helpCommand.getSuperCommand().orElseThrow(
            () -> new IllegalStateException(
                "HelpCommand " + helpCommand.getNameKey() + " does not have a supercommand!"));

        final @NonNull ICommandSender commandSender = commandResult.getCommandSender();

        commandSender.sendMessage(renderHelpText(commandSender, commandSender.getColorScheme(), superCommand,
                                                 helpCommand.helpCommandRenderer,
                                                 commandResult.getParsedArgument("helpArg")));
    }
}
