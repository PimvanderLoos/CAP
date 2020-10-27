package nl.pim16aap2.cap.command;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandParserException;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.util.CheckedConsumer;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a command that can be parsed from the CLI.
 *
 * @author Pim
 */
public class Command
{
    /**
     * The default help argument to use in case that is required.
     */
    static final @NonNull Argument<Boolean> DEFAULT_HELP_ARGUMENT =
        Argument.valuesLessBuilder().name("h").longName("help")
                .summary("Displays the help menu for this command.").build();

    /**
     * The name of this command.
     */
    @Getter
    protected final @NonNull String name;

    /**
     * The number of subcommands this command has.
     */
    private @Nullable Integer subCommandCount = null;

    /**
     * The list of subcommands this command has.
     */
    @Getter
    protected final @NonNull List<@NonNull Command> subCommands;

    /**
     * The {@link ArgumentManager} that manages all the arguments this command has.
     */
    @Getter
    protected final @NonNull ArgumentManager argumentManager;

    /**
     * The function that will be executed by {@link CommandResult#run()}.
     */
    @Getter
    protected final @NonNull CheckedConsumer<@NonNull CommandResult, CommandParserException> commandExecutor;

    /**
     * The description of the command. This is the longer description shown in the help menu for this command.
     */
    @Setter
    protected @Nullable String description;

    /**
     * The summary of the command. This is the short description shown in the list of commands.
     */
    @Setter
    protected @Nullable String summary;

    /**
     * The header of the command. This is text shown at the top of the help menu for this command.
     */
    @Setter
    protected @Nullable String header;

    /**
     * The supplier that is used to build the description. Note that this isn't used in case the {@link #description} is
     * not null.
     */
    @Setter
    protected @Nullable Function<ColorScheme, String> descriptionSupplier;

    /**
     * The supplier that is used to build the summary. Note that this isn't used in case the {@link #summary} is not
     * null.
     */
    @Setter
    protected @Nullable Function<ColorScheme, String> summarySupplier;

    /**
     * The supplier that is used to build the summary. Note that this isn't used in case the {@link #header} is not
     * null.
     */
    @Setter
    protected @Nullable Function<ColorScheme, String> headerSupplier;

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
     * Whether this command is hidden or not. Hidden commands will not show up in help menus.
     */
    @Setter
    @Getter
    protected boolean hidden;

    /**
     * The permission value to use to determine if an {@link ICommandSender} has access to this command or not. See
     * {@link ICommandSender#hasPermission(Command)}.
     */
    @Getter
    protected @Nullable String permission;

    /**
     * @param name                     The name of the command.
     * @param description              The description of the command. This is the longer description shown in the help
     *                                 menu for this command.
     * @param descriptionSupplier      The supplier that is used to build the description. Note that this isn't used in
     *                                 case the description is provided.
     * @param summary                  The summary of the command. This is the short description shown in the list of
     *                                 commands.
     * @param summarySupplier          The supplier that is used to build the summary. Note that this isn't used in case
     *                                 a summary is provided.
     * @param header                   The header of the command. This is text shown at the top of the help menu for
     *                                 this command.
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
     * @param commandExecutor          The function that will be executed by {@link CommandResult#run()}.
     * @param arguments                The list of {@link Argument}s accepted by this command.
     * @param hidden                   Whether this command is hidden or not. Hidden commands will not show up in help
     *                                 menus.
     * @param cap                      The {@link CAP} instance that manages this command.
     * @param permission               The permission value to use to determine if an {@link ICommandSender} has access
     *                                 to this command or not. See {@link ICommandSender#hasPermission(Command)}.
     */
    @Builder(builderMethodName = "commandBuilder")
    protected Command(final @NonNull String name, final @Nullable String description,
                      final @Nullable Function<ColorScheme, String> descriptionSupplier, final @Nullable String summary,
                      final @Nullable Function<ColorScheme, String> summarySupplier, final @Nullable String header,
                      final @Nullable Function<ColorScheme, String> headerSupplier,
                      final @Nullable @Singular List<Command> subCommands, final @Nullable Command helpCommand,
                      final @Nullable Boolean addDefaultHelpSubCommand, @Nullable Argument<?> helpArgument,
                      final @Nullable Boolean addDefaultHelpArgument,
                      final @NonNull CheckedConsumer<@NonNull CommandResult, CommandParserException> commandExecutor,
                      @Nullable @Singular(ignoreNullCollections = true) List<@NonNull Argument<?>> arguments,
                      final boolean hidden, final @NonNull CAP cap, final @Nullable String permission)
    {
        this.name = name;

        this.description = description;
        this.descriptionSupplier = descriptionSupplier;

        this.summary = summary;
        this.summarySupplier = summarySupplier;

        this.header = header;
        this.headerSupplier = headerSupplier;

        // If there are no subcommands, make an empty list. If there are subcommands, put them in a modifiable list.
        this.subCommands = subCommands == null ? new ArrayList<>(0) : new ArrayList<>(subCommands);
        this.commandExecutor = commandExecutor;
        this.hidden = hidden;

        this.permission = permission;

        this.helpCommand = helpCommand;
        if (helpCommand == null && Util.valOrDefault(addDefaultHelpSubCommand, false))
            this.helpCommand = DefaultHelpCommand.getDefault(cap);
        if (this.helpCommand != null)
            this.subCommands.add(0, this.helpCommand);

        this.subCommands.forEach(subCommand -> subCommand.superCommand = Optional.of(this));
        this.cap = cap;

        this.helpArgument = helpArgument;
        if (helpArgument == null && Util.valOrDefault(addDefaultHelpArgument, false))
            this.helpArgument = DEFAULT_HELP_ARGUMENT;

        // If there are no arguments, make an empty list. If there are arguments, put them in a modifiable list.
        arguments = arguments == null ? new ArrayList<>(0) : new ArrayList<>(arguments);
        // Add the help argument to the list.
        if (this.helpArgument != null)
            arguments.add(this.helpArgument);

        argumentManager = new ArgumentManager(arguments);
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
        for (final @NonNull Command command : subCommands)
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
     * Gets the description for this command.
     * <p>
     * First, it tries to return {@link #description}. If that is null, {@link #descriptionSupplier} is used instead. If
     * that is null as well, an empty String is returned.
     *
     * @param colorScheme The {@link ColorScheme} to use in case {@link #descriptionSupplier} is needed.
     * @return The description for this command if it could be found/generated, otherwise an empty String.
     */
    public @NonNull String getDescription(final @NonNull ColorScheme colorScheme)
    {
        if (description != null)
            return description;
        if (descriptionSupplier != null)
            return descriptionSupplier.apply(colorScheme);
        return "";
    }

    /**
     * Gets the summary for this command.
     * <p>
     * First, it tries to return {@link #summary}. If that is null, {@link #summarySupplier} is used instead. If that is
     * null as well, an empty String is returned.
     *
     * @param colorScheme The {@link ColorScheme} to use in case {@link #summarySupplier} is needed.
     * @return The summary for this command if it could be found/generated, otherwise an empty String.
     */
    public @NonNull String getSummary(final @NonNull ColorScheme colorScheme)
    {
        if (summary != null)
            return summary;
        if (summarySupplier != null)
            return summarySupplier.apply(colorScheme);
        return "";
    }

    /**
     * Gets the header for this command.
     * <p>
     * First, it tries to return {@link #header}. If that is null, {@link #headerSupplier} is used instead. If that is
     * null as well, an empty String is returned.
     *
     * @param colorScheme The {@link ColorScheme} to use in case {@link #summarySupplier} is needed.
     * @return The header for this command if it could be found/generated, otherwise an empty String.
     */
    public @NonNull String getHeader(final @NonNull ColorScheme colorScheme)
    {
        if (header != null)
            return header;
        if (headerSupplier != null)
            return headerSupplier.apply(colorScheme);
        return "";
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
                  .renderLongCommand(commandSender, commandSender.getColorScheme(), this);
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
     * Searches for a sub{@link Command} of a given type.
     *
     * @param clazz The {@link Class} to search for.
     * @param <T>   The Type of the sub{@link Command} to find.
     * @return An {@link Optional} containing the sub{@link Command}.
     */
    public @NonNull <T> Optional<Command> getSubCommand(final @NonNull Class<T> clazz)
    {
        return Util.searchIterable(subCommands, (val) -> clazz.isAssignableFrom(val.getClass()));
    }

    /**
     * Searches for a sub{@link Command} with a given name.
     *
     * @param name The name of the sub{@link Command} to look for.
     * @return An optional containing the sub{@link Command} with the given name, if it exists, otherwise {@link
     * Optional#empty()}.
     */
    public @NonNull Optional<Command> getSubCommand(final @Nullable String name)
    {
        if (name == null)
            return Optional.empty();
        return Util.searchIterable(subCommands, (val) -> val.getName().equals(name));
    }
}
