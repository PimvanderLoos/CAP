package nl.pim16aap2.commandparser.command;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.commandsender.ICommandSender;
import nl.pim16aap2.commandparser.exception.CommandParserException;
import nl.pim16aap2.commandparser.manager.ArgumentManager;
import nl.pim16aap2.commandparser.manager.CommandManager;
import nl.pim16aap2.commandparser.text.ColorScheme;
import nl.pim16aap2.commandparser.text.Text;
import nl.pim16aap2.commandparser.util.CheckedConsumer;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Getter
public class Command
{
    static final @NonNull Argument<Boolean> DEFAULT_HELP_ARGUMENT =
        Argument.valuesLessBuilder().name("h").longName("help")
                .summary("Displays the help menu for this command.").build();

    protected final @NonNull String name;


    protected final @NonNull List<@NonNull Command> subCommands;

    protected final @NonNull ArgumentManager argumentManager;

    protected final @NonNull CheckedConsumer<@NonNull CommandResult, CommandParserException> commandExecutor;

    @Setter
    @Getter(AccessLevel.PROTECTED)
    protected @Nullable String description;
    @Setter
    @Getter(AccessLevel.PROTECTED)
    protected @Nullable String summary;
    @Setter
    @Getter(AccessLevel.PROTECTED)
    protected @Nullable String header;
    @Setter
    @Getter(AccessLevel.PROTECTED)
    protected @Nullable Function<ColorScheme, String> descriptionSupplier;
    @Setter
    @Getter(AccessLevel.PROTECTED)
    protected @Nullable Function<ColorScheme, String> summarySupplier;
    @Setter
    @Getter(AccessLevel.PROTECTED)
    protected @Nullable Function<ColorScheme, String> headerSupplier;

    /**
     * The {@link CommandManager} that manages this command.
     */
    protected final @NonNull CommandManager commandManager;

    private Optional<Command> superCommand = Optional.empty();

    private Command helpCommand;

    private Argument<?> helpArgument;

    @Setter
    protected boolean hidden;

    protected boolean addDefaultHelpArgument;

    @Builder(builderMethodName = "commandBuilder")
    protected Command(final @NonNull String name,
                      final @Nullable String description,
                      final @Nullable Function<ColorScheme, String> descriptionSupplier,
                      final @Nullable String summary, final @Nullable Function<ColorScheme, String> summarySupplier,
                      final @Nullable String header, final @Nullable Function<ColorScheme, String> headerSupplier,
                      final @Nullable @Singular List<Command> subCommands, final @Nullable Command helpCommand,
                      final @NonNull CheckedConsumer<@NonNull CommandResult, CommandParserException> commandExecutor,
                      @Nullable @Singular(ignoreNullCollections = true) List<@NonNull Argument<?>> arguments,
                      @Nullable Argument<?> helpArgument, final boolean hidden,
                      final @NonNull CommandManager commandManager, final @Nullable Boolean addDefaultHelpArgument,
                      final @Nullable Boolean addDefaultHelpSubCommand)
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


        this.helpCommand = helpCommand;
        if (helpCommand == null && Util.valOrDefault(addDefaultHelpSubCommand, false))
            this.helpCommand = DefaultHelpCommand.getDefault(commandManager);
        if (this.helpCommand != null)
            this.subCommands.add(0, this.helpCommand);

        this.subCommands.forEach(subCommand -> subCommand.superCommand = Optional.of(this));
        this.commandManager = commandManager;
        this.addDefaultHelpArgument = Util.valOrDefault(addDefaultHelpArgument, false);

        this.helpArgument = helpArgument;
        if (helpArgument == null && Util.valOrDefault(addDefaultHelpArgument, false))
            this.helpArgument = DEFAULT_HELP_ARGUMENT;

        // If there are no arguments, make an empty list. If there are arguments, put them in a modifiable list.
        arguments = arguments == null ? new ArrayList<>(0) : new ArrayList<>(arguments);
        // Add the help argument to the list.
        if (this.helpArgument != null)
            arguments.add(this.helpArgument);

        this.argumentManager = new ArgumentManager(arguments);
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
     * CommandManager#getHelpCommandRenderer()}.
     *
     * @param commandSender The {@link ICommandSender} that is used to generate the help message (i.e. using their
     *                      {@link ColorScheme}).
     * @return The generated help message.
     */
    public @NonNull Text getHelp(final @NonNull ICommandSender commandSender)
    {
        return commandManager.getHelpCommandRenderer().renderLongCommand(commandSender.getColorScheme(), this);
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
     * Searches for a sub{@link Command} of a given types.
     *
     * @param clazz The {@link Class} to search for.
     * @param <T>   The Type of the sub{@link Command} to find.
     * @return An {@link Optional} containing the sub{@link Command}.
     */
    public @NonNull <T> Optional<Command> getSubCommand(final @NonNull Class<T> clazz)
    {
        return Util.searchIterable(subCommands, (val) -> clazz.isAssignableFrom(val.getClass()));
    }

    public @NonNull Optional<Command> getSubCommand(final @Nullable String name)
    {
        if (name == null)
            return Optional.empty();
        return Util.searchIterable(subCommands, (val) -> val.getName().equals(name));
    }
}
