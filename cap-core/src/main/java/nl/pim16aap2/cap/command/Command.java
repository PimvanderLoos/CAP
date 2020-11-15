package nl.pim16aap2.cap.command;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.IntegerArgument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CAPException;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.util.Functional.CheckedConsumer;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a command that can be parsed from the CLI.
 *
 * @author Pim
 */
public class Command
{
    /**
     * The default help argument to use in case that is required. This is the non-localized version. If you want to
     * enable localization, see {@link #DEFAULT_HELP_ARGUMENT_LOCALIZED}.
     */
    static final @NonNull Argument<Boolean> DEFAULT_HELP_ARGUMENT =
        Argument.valuesLessBuilder().shortName("h").longName("help").identifier("help")
                .summary("Displays the help menu for this command.").build();

    /**
     * The default help argument to use in case that is required. This is the localized version. If you want to get a
     * non-localized version, see {@link #DEFAULT_VIRTUAL_ARGUMENT}.
     */
    static final @NonNull Argument<Boolean> DEFAULT_HELP_ARGUMENT_LOCALIZED =
        Argument.valuesLessBuilder().identifier("help")
                .shortName("default.helpArgument.shortName")
                .longName("default.helpArgument.longName")
                .summary("default.helpArgument.summary").build();

    /**
     * The default virtual {@link Argument} to use for {@link #virtual} {@link Command}s. This is the non-localized
     * version. If you want to * enable localization, see {@link #DEFAULT_VIRTUAL_ARGUMENT_LOCALIZED}.
     */
    static final @NonNull Argument<Integer> DEFAULT_VIRTUAL_ARGUMENT =
        new IntegerArgument().getOptionalPositional().shortName("page").identifier("page")
                             .summary("The page number of the help menu to display").build();

    /**
     * The default virtual {@link Argument} to use for {@link #virtual} {@link Command}s. This is the localized version.
     * If you want to get a * non-localized version, see {@link #DEFAULT_VIRTUAL_ARGUMENT}.
     */
    static final @NonNull Argument<Integer> DEFAULT_VIRTUAL_ARGUMENT_LOCALIZED =
        new IntegerArgument().getOptionalPositional().shortName("default.virtualArgument.shortName").identifier("page")
                             .summary("default.virtualArgument.summary").build();

    /**
     * The {@link CommandNamingSpec} for this {@link Command}.
     */
    protected final @NonNull CommandNamingSpec nameSpec;

    /**
     * The number of subcommands this command has.
     */
    private @Nullable Integer subCommandCount = null;

    /**
     * The list of subcommands this command has.
     */
    protected final @NonNull CommandMap subCommands;

    /**
     * The {@link ArgumentManager} that manages all the arguments this command has.
     */
    @Getter
    protected final @NonNull ArgumentManager argumentManager;

    /**
     * The function that will be executed by {@link CommandResult#run()}. This value is ignored when {@link #virtual} is
     * enabled. Otherwise, it's required.
     */
    @Getter
    protected final @NonNull CheckedConsumer<@NonNull CommandResult, CAPException> commandExecutor;

    /**
     * The supplier that is used to build the description. Note that this isn't used in case the {@link
     * CommandNamingSpec#getDescription(CAP, Locale)} is not null.
     */
    @Setter
    protected @Nullable Function<ICommandSender, String> descriptionSupplier;

    /**
     * The supplier that is used to build the summary. Note that this isn't used in case the {@link
     * CommandNamingSpec#getSummary(CAP, Locale)} is not null.
     */
    @Setter
    protected @Nullable Function<ICommandSender, String> summarySupplier;

    /**
     * The supplier that is used to build the summary. Note that this isn't used in case the {@link
     * CommandNamingSpec#getHeader(CAP, Locale)} is not null.
     */
    @Setter
    protected @Nullable Function<ICommandSender, String> headerSupplier;

    /**
     * The {@link CAP} that manages this command.
     */
    @Getter
    protected final @NonNull CAP cap;

    /**
     * The supercommand of this command. If it doesn't exist, this command is treated as a top level command.
     */
    @Getter
    protected @NonNull Optional<Command> superCommand = Optional.empty();

    /**
     * The helpcommand to use. This is used in case of '/command help [subcommand]'. If no subcommands are provided,
     * this will not be used.
     */
    @Getter
    protected @Nullable Command helpCommand;

    /**
     * The help argument to use. This is used in the format '/command [-h / --help]'.
     */
    @Getter
    protected @Nullable Argument<?> helpArgument;

    /**
     * Whether this command is virtual or not. Hidden commands will not show up in help menus. Virtual commands are
     * assigned the virtual command executor, see {@link #virtualCommandExecutor(CommandResult)}, which delegates
     * numerical optional positional arguments (see {@link Command#DEFAULT_VIRTUAL_ARGUMENT}) to the help command
     * renderer.
     */
    @Getter
    protected boolean virtual;

    /**
     * The permission function to determine if an {@link ICommandSender} has access to this command or not.
     * <p>
     * When this is null, it defaults to <i>true</i>.
     */
    @Getter
    protected @Nullable BiFunction<ICommandSender, Command, Boolean> permission;

    /**
     * @param nameSpec                 See {@link #nameSpec}.
     * @param descriptionSupplier      The supplier that is used to build the description. Note that this isn't used in
     *                                 case the description is provided.
     * @param summarySupplier          The supplier that is used to build the summary. Note that this isn't used in case
     *                                 a summary is provided.
     * @param headerSupplier           The supplier that is used to build the header. Note that this isn't used in case
     *                                 a header is provided.
     * @param subCommands              The list of subcommands this command will be the supercommand of.
     * @param helpCommand              The helpcommand to use. This is used in case of '/command help [subcommand]'. If
     *                                 no subcommands are provided, this will not be used.
     * @param addDefaultHelpSubCommand Whether to add the default help command as subcommand. See {@link
     *                                 DefaultHelpCommand}. Note that this has no effect if you specified your own
     *                                 helpCommand.
     * @param helpArgument             The help argument to use. This is used in the format '/command [-h / --help]'.
     * @param addDefaultHelpArgument   Whether to add the default help argument. See {@link #DEFAULT_HELP_ARGUMENT}.
     *                                 Note that this has no effect if you specified your own helpArgument.
     * @param commandExecutor          The function that will be executed by {@link CommandResult#run()}. This value is
     *                                 ignored when {@link #virtual} is enabled. Otherwise, it's required.
     * @param arguments                The list of {@link Argument}s accepted by this command.
     * @param virtual                  Whether this command is virtual or not. Hidden commands will not show up in help
     *                                 menus. Virtual commands are assigned the virtual command executor, see {@link
     *                                 #virtualCommandExecutor(CommandResult)}, which delegates numerical optional
     *                                 positional arguments (see {@link Command#DEFAULT_VIRTUAL_ARGUMENT}) to the help
     *                                 command renderer.
     * @param cap                      The {@link CAP} instance that manages this command.
     * @param permission               The permission function to use to determine if an {@link ICommandSender} has
     *                                 access to this command or not.
     */
    @Builder(builderMethodName = "commandBuilder")
    protected Command(final @NonNull CommandNamingSpec nameSpec,
                      final @Nullable Function<ICommandSender, String> descriptionSupplier,
                      final @Nullable Function<ICommandSender, String> summarySupplier,
                      final @Nullable Function<ICommandSender, String> headerSupplier,
                      final @Nullable @Singular List<Command> subCommands, final @Nullable Command helpCommand,
                      final @Nullable Boolean addDefaultHelpSubCommand, @Nullable Argument<?> helpArgument,
                      final @Nullable Boolean addDefaultHelpArgument,
                      final @Nullable CheckedConsumer<@NonNull CommandResult, CAPException> commandExecutor,
                      @Nullable @Singular(ignoreNullCollections = true) List<@NonNull Argument<?>> arguments,
                      final boolean virtual, final @NonNull CAP cap,
                      final @Nullable BiFunction<ICommandSender, Command, Boolean> permission)
    {
        if (commandExecutor == null && !virtual)
            throw new IllegalArgumentException("CommandExecutor may not be null for non-virtual commands!");

        this.nameSpec = nameSpec;
        nameSpec.verify(cap);

        this.descriptionSupplier = descriptionSupplier;

        this.summarySupplier = summarySupplier;

        this.headerSupplier = headerSupplier;

        this.subCommands = new CommandMap(cap);
        this.commandExecutor = virtual ? Command::virtualCommandExecutor : commandExecutor;
        this.virtual = virtual;

        this.permission = permission;

        this.helpCommand = helpCommand;
        if (helpCommand == null && Util.valOrDefault(addDefaultHelpSubCommand, false))
            this.helpCommand = DefaultHelpCommand.getDefault(cap, cap.localizationEnabled());
        if (this.helpCommand != null)
            this.subCommands.addCommand(this.helpCommand);

        if (subCommands != null)
            subCommands.forEach(this.subCommands::addCommand);

        this.subCommands.getLocaleMap().values().forEach(subCommand -> subCommand.superCommand = Optional.of(this));
        this.cap = cap;

        this.helpArgument = helpArgument;
        if (helpArgument == null && Util.valOrDefault(addDefaultHelpArgument, false))
            this.helpArgument = cap.localizationEnabled() ? DEFAULT_HELP_ARGUMENT_LOCALIZED : DEFAULT_HELP_ARGUMENT;

        // If there are no arguments, make an empty list. If there are arguments, put them in a modifiable list.
        arguments = arguments == null ? new ArrayList<>(0) : new ArrayList<>(arguments);
        // Add the help argument to the list.
        if (this.helpArgument != null)
            arguments.add(this.helpArgument);
        if (this.virtual)
            arguments.add(cap.localizationEnabled() ? DEFAULT_VIRTUAL_ARGUMENT_LOCALIZED : DEFAULT_VIRTUAL_ARGUMENT);

        argumentManager = new ArgumentManager(cap, arguments, cap.isCaseSensitive());
    }

    /**
     * The default {@link #commandExecutor} for {@link #virtual} {@link Command}s.
     * <p>
     * If will display the help page with the value determined by the integer argument named "page". If this argument is
     * not provided, the first page will be displayed.
     *
     * @param commandResult The {@link CommandResult} to use for sending the help menu of this virtual command.
     * @throws CAPException If a CAP-related issue occurred.
     */
    public static void virtualCommandExecutor(final @NonNull CommandResult commandResult)
        throws CAPException
    {
        final int page = Util.valOrDefault(commandResult.getParsedArgument("page"), 1);
        commandResult.sendSubcommandHelp(page);
    }

    /**
     * Gets the top level command of this command. This basically means to traverse up the command tree until we
     * encounter a {@link Command} that does not have a super command.
     *
     * @return The top level {@link Command} of this {@link Command}.
     */
    public @NonNull Command getTopLevelCommand()
    {
        return getSuperCommand().map(Command::getTopLevelCommand).orElse(this);
    }

    /**
     * Gets the total number of sub{@link Command}s this {@link Command} has, including the number of sub{@link
     * Command}s for ever sub{@link Command}.
     *
     * @return The total number of sub{@link Command}s this {@link Command} has.
     */
    public final int getSubCommandCount()
    {
        return subCommandCount == null ? subCommandCount = calculateSubCommandCount() : subCommandCount;
    }

    /**
     * Recursively counts the number of sub{@link Command}s this {@link Command} has.
     *
     * @return The number of sub{@link Command}s this {@link Command} has.
     */
    private int calculateSubCommandCount()
    {
        int count = 0;
        for (final @NonNull Command command : subCommands.getLocaleMap().values())
            count += command.getSubCommandCount() + 1;
        return count;
    }

    /**
     * Invalidates the {@link #subCommandCount} for this {@link Command} as well as its super {@link Command}s
     * (recursively).
     */
    private void invalidateSubCommandCount()
    {
        subCommandCount = null;
        getSuperCommand().ifPresent(Command::invalidateSubCommandCount);
    }

    /**
     * Gets the identifier for this {@link Command}. For example "example.command" (when localized) or "mycommand" (when
     * using raw strings).
     *
     * @return THe identifier for this {@link Command}.
     */
    public @NonNull String getIdentifier()
    {
        return nameSpec.getIdentifier();
    }

    /**
     * Gets the name of this command.
     *
     * @return The name of this command.
     */
    public @NonNull String getName(final @Nullable Locale locale)
    {
        return nameSpec.getName(cap, locale);
    }

    /**
     * Gets the description for this command.
     * <p>
     * First, it tries to return {@link CommandNamingSpec#getName(CAP, Locale)}. If that is null, {@link
     * #descriptionSupplier} is used instead. If that is null as well, an empty String is returned.
     *
     * @param commandSender The {@link ICommandSender} to use in case {@link #descriptionSupplier} is needed.
     * @return The description for this command if it could be found/generated, otherwise an empty String.
     */
    public @NonNull String getDescription(final @NonNull ICommandSender commandSender)
    {
        return Util.firstNonNull("",
                                 nameSpec.getDescription(cap, commandSender.getLocale()),
                                 (descriptionSupplier == null ? null : descriptionSupplier.apply(commandSender)));
    }

    /**
     * Gets the summary for this command.
     * <p>
     * First, it tries to return {@link CommandNamingSpec#getSummary(CAP, Locale)}. If that is null, {@link
     * #summarySupplier} is used instead. If that is null as well, an empty String is returned.
     *
     * @param commandSender The {@link ICommandSender} to use in case {@link #summarySupplier} is needed.
     * @return The summary for this command if it could be found/generated, otherwise an empty String.
     */
    public @NonNull String getSummary(final @NonNull ICommandSender commandSender)
    {
        return Util.firstNonNull("",
                                 nameSpec.getSummary(cap, commandSender.getLocale()),
                                 (summarySupplier == null ? null : summarySupplier.apply(commandSender)));
    }

    /**
     * Gets the header for this command.
     * <p>
     * First, it tries to return {@link CommandNamingSpec#getHeader(CAP, Locale)}. If that is null, {@link
     * #headerSupplier} is used instead. If that is null as well, an empty String is returned.
     *
     * @param commandSender The {@link ICommandSender} to use in case {@link #summarySupplier} is needed.
     * @return The header for this command if it could be found/generated, otherwise an empty String.
     */
    public @NonNull String getHeader(final @NonNull ICommandSender commandSender)
    {
        return Util.firstNonNull("",
                                 nameSpec.getHeader(cap, commandSender.getLocale()),
                                 (headerSupplier == null ? null : headerSupplier.apply(commandSender)));
    }

    /**
     * The title of the section for the command-specific help menu.
     *
     * @param commandSender The {@link ICommandSender} to use (for localization)
     * @return The section title for this {@link Command}.
     */
    public @NonNull String getSectionTitle(final @NonNull ICommandSender commandSender)
    {
        return Util.valOrDefault(nameSpec.getSectionTitle(cap, commandSender.getLocale()), "");
    }

    /**
     * Generates the help message for this {@link Command} for the given {@link ICommandSender} using {@link
     * CAP#getHelpCommandRenderer()}.
     *
     * @param commandSender The {@link ICommandSender} that is used to generate the help message (i.e. using their
     *                      {@link ColorScheme}).
     * @return The generated help message.
     */
    public @NonNull Text getHelp(final @NonNull ICommandSender commandSender)
    {
        return cap.getHelpCommandRenderer()
                  .renderHelpMenu(commandSender, commandSender.getColorScheme(), this);
    }

    /**
     * Send the help message generated by {@link #getHelp(ICommandSender)} to the specified {@link ICommandSender}.
     *
     * @param commandSender The {@link ICommandSender} that will receive the help message.
     */
    public void sendHelp(final @NonNull ICommandSender commandSender)
    {
        commandSender.sendMessage(getHelp(commandSender));
    }

    /**
     * Searches for a sub{@link Command} with a given name.
     *
     * @param name   The name of the sub{@link Command} to look for.
     * @param locale The {@link Locale} for which to get the {@link Command}.
     * @return An optional containing the sub{@link Command} with the given name, if it exists, otherwise {@link
     * Optional#empty()}.
     */
    public @NonNull Optional<Command> getSubCommand(final @Nullable String name, final @Nullable Locale locale)
    {
        return subCommands.getCommand(name, locale);
    }

    /**
     * Checks if a given {@link ICommandSender} has permission to use this command.
     * <p>
     * See {@link #permission}.
     *
     * @param commandSender The {@link ICommandSender} whose permission status to check.
     * @return True if the {@link ICommandSender} has access to this command.
     */
    public boolean hasPermission(final @NonNull ICommandSender commandSender)
    {
        if (permission == null)
            return true;
        return permission.apply(commandSender, this);
    }

    /**
     * Gets all the sub{@link Command}s of this {@link Command}.
     *
     * @return All the sub{@link Command}s.
     */
    public @NonNull Collection<@NonNull Command> getSubCommands()
    {
        return subCommands.getLocaleMap().values();
    }

    /**
     * Gets the names of all the sub{@link Command}s of this {@link Command} for a specific locale.
     *
     * @return The names of all the sub{@link Command}s.
     */
    public @NonNull Collection<@NonNull String> getSubCommandNames(final @Nullable Locale locale)
    {
        return subCommands.getLocaleMap(locale).keySet();
    }
}
