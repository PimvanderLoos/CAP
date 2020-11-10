package nl.pim16aap2.cap.commandparser;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.validator.IArgumentValidator;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.exception.MissingArgumentException;
import nl.pim16aap2.cap.exception.NoPermissionException;
import nl.pim16aap2.cap.exception.NonExistingArgumentException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This class is used to parse commands and their arguments into a single {@link CommandResult} and to generate
 * suggestions.
 *
 * @author Pim
 */
public class CommandParser
{
    /**
     * The {@link CAP} instance that controls this command parser.
     */
    protected final @NonNull CAP cap;

    /**
     * The {@link ICommandSender} for which to parse the {@link #input}.
     */
    protected final @NonNull ICommandSender commandSender;

    /**
     * The prefix used for free {@link Argument}s.
     */
    protected static final char ARGUMENT_PREFIX = '-';

    /**
     * The pattern for leading {@link #ARGUMENT_PREFIX}es.
     */
    protected static final Pattern LEADING_PREFIX_PATTERN = Pattern.compile("^[-]{1,2}");

    /**
     * Whether or not free {@link Argument}s are separated from their values using spaces or not.
     * <p>
     * Example for true: "--player pim16aap2".
     * <p>
     * Example for false: "--player=pim16aap2".
     */
    @Getter
    protected boolean spaceSeparated;

    /**
     * The separator between free {@link Argument}s are separated from their values.
     * <p>
     * For example, when this is '=', the resulting input for a free argument could be: "--player=pim16aap2".
     */
    protected @NonNull String separator;

    /**
     * The pattern for the {@link #separator}.
     */
    protected @NonNull Pattern separatorPattern;

    /**
     * The {@link CommandLineInput} containing the input to parse.
     */
    protected @NonNull CommandLineInput input;

    protected CommandParser(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                            final @NonNull CommandLineInput input, final char separator)
    {
        this.separator = Character.toString(separator);
        separatorPattern = Pattern.compile(this.separator);
        spaceSeparated = this.separator.equals(" ");
        this.input = input;
        this.commandSender = commandSender;
        this.cap = cap;
    }

    /**
     * Constructs a new command parser.
     *
     * @param cap           The {@link CAP} instance that owns this object..
     * @param commandSender The {@link ICommandSender} that issued the command.
     * @param input         The string that may contain a set of commands and arguments.
     * @param separator     The separator between a free argument's flag and its value. E.g. '<i>=</i>' for the format
     *                      <i>'--player=pim16aap2'</i>.
     */
    public CommandParser(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                         final @NonNull String input, final char separator)
        throws EOFException
    {
        this(cap, commandSender, new CommandLineInput(input), separator);

        if (!this.input.isCompleteQuotationMarks())
            throw new EOFException();
    }

    /**
     * Gets the list of entries as parsed from the input string.
     *
     * @return The list of arguments.
     */
    public @NonNull List<@NonNull String> getArgs()
    {
        return input.getArgs();
    }

    /**
     * Gets the argument name and its value for the last argument in a list of arguments.
     *
     * @return The name and value of the last argument.
     */
    public @NonNull Pair<String, String> getLastArgumentData()
    {
        final String[] parts = input.getArgs().get(input.size() - 1).split(separator, 2);
        if (parts.length == 2)
            return new Pair<>(parts[0] + separator, parts[1]);
        return new Pair<>("", parts[0]);

    }

    /**
     * Strips up to two leading {@link #ARGUMENT_PREFIX}es from a String if any could be found.
     * <p>
     * For example, when provided with '-a', it will return 'a' and for '--admin-' it would be 'admin-'. '---admin',
     * however, would return '-admin'.
     * <p>
     * If the argument does not have at least 1 leading {@link #ARGUMENT_PREFIX}, {@link Optional#empty()} is returned.
     *
     * @param argument The name of the argument with one or more {@link #ARGUMENT_PREFIX}es.
     * @return The argument without the first 1 or 2 leading {@link #ARGUMENT_PREFIX}es if the argument contained any.
     */
    static @NonNull Optional<String> lStripArgumentPrefix(final @NonNull String argument)
    {
        if (argument.length() > 0 && argument.charAt(0) != ARGUMENT_PREFIX)
            return Optional.empty();

        return Optional.of(LEADING_PREFIX_PATTERN.matcher(argument).replaceFirst(""));
    }

    /**
     * Parses the arguments.
     *
     * @return The result of parsing the argument.
     *
     * @throws CommandNotFoundException     If a specified command could not be found.
     * @throws NonExistingArgumentException If one of the specified arguments does not exist.
     * @throws MissingArgumentException     If a required argument was not specified.
     * @throws NoPermissionException        If the {@link ICommandSender} does not have permission to use this command.
     *                                      See {@link Command#hasPermission(ICommandSender)}.
     * @throws ValidationFailureException   If the value of an {@link Argument} could not be validated. See {@link
     *                                      IArgumentValidator#validate(Object)}.
     * @throws IllegalValueException        If the specified value of an {@link Argument} is illegal.
     */
    // TODO: What's the difference between an IllegalValue and a ValidationFailure, exactly?
    public @NonNull CommandResult parse()
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException, NoPermissionException,
               ValidationFailureException, IllegalValueException
    {
        final @NonNull ParsedCommand parsedCommand = getLastCommand();
        if (!parsedCommand.getCommand().hasPermission(commandSender))
            throw new NoPermissionException(commandSender, parsedCommand.getCommand(), cap.isDebug());

        if (parsedCommand.getIndex() == (input.size() - 1) &&
            parsedCommand.getCommand().getArgumentManager().getRequiredArguments().size() > 0)
            return new CommandResult(commandSender, parsedCommand.getCommand());

        return new CommandResult(commandSender, parsedCommand.getCommand(),
                                 parseArguments(parsedCommand.getCommand(),
                                                parsedCommand.getIndex()));
    }

    /**
     * Parses all the {@link Argument}s for a given {@link Command}.
     *
     * @param command The {@link Command} to parse the {@link Argument}s for.
     * @param idx     The index of the {@link Command} in {@link #input}. All values with a higher index than this will
     *                be processed as {@link Argument}s.
     * @return The map of {@link Argument.IParsedArgument}s resulting from parsing the input. Any missing optional
     * {@link Argument}s with default values will be assigned their default value. {@link Argument#getIdentifier()} is
     * used for the keys in the map.
     *
     * @throws NonExistingArgumentException If one of the specified arguments does not exist.
     * @throws MissingArgumentException     If a required argument was not specified.
     * @throws ValidationFailureException   If the value of an {@link Argument} could not be validated. See {@link
     *                                      IArgumentValidator#validate(Object)}.
     * @throws IllegalValueException        If the specified value of an {@link Argument} is illegal.
     */
    private @Nullable Map<@NonNull String, Argument.IParsedArgument<?>> parseArguments(final @NonNull Command command,
                                                                                       final int idx)
        throws NonExistingArgumentException, MissingArgumentException, ValidationFailureException, IllegalValueException
    {
        final @NonNull Map<@NonNull String, Argument.IParsedArgument<?>> results = new HashMap<>();

        int requiredArgumentIdx = 0;
        for (int pos = idx + 1; pos < input.size(); ++pos)
        {
            final String nextArg = input.getArgs().get(pos);
            final @NonNull Argument<?> argument;
            @NonNull String value;
            if (nextArg.charAt(0) == ARGUMENT_PREFIX)
            {
                // If the second character is also an ARGUMENT_PREFIX, then the long name is used
                // If this is the case, then we simply have to start reading the argument name
                // 1 position later than for the short name (i.e. '-a' vs '--admin'.
                final int argNameStartIdx = nextArg.charAt(1) == ARGUMENT_PREFIX ? 2 : 1;
                final String[] parts = separatorPattern.split(nextArg, 2);

                final @NonNull String argumentName = parts[0].substring(argNameStartIdx);
                argument = command.getArgumentManager().getArgument(argumentName)
                                  .orElseThrow(
                                      () -> new NonExistingArgumentException(command, argumentName,
                                                                             cap.isDebug()));

                if (argument.isValuesLess())
                    value = "";
                else
                {
                    // When the separator is a space, the value isn't stored in the second part of the
                    // current entry. The input is split on spaces and as such, it is stored in the next arg.
                    // So, we get the next arg and increment the current position by 1 to indicated we've
                    // just processed it.
                    final int nextPos = pos + 1;
                    if (spaceSeparated)
                    {
                        value = input.size() >= nextPos ? input.getArgs().get(nextPos) : "";
                        pos += 1;
                    }
                    else
                        value = parts[1];
                }
            }
            else
            {
                final int currentRequiredArgumentIdx = requiredArgumentIdx;
                argument = command.getArgumentManager().getPositionalArgumentAtIdx(currentRequiredArgumentIdx)
                                  .orElseThrow(
                                      () -> new NonExistingArgumentException(command,
                                                                             "Missing required argument at pos: " +
                                                                                 currentRequiredArgumentIdx,
                                                                             cap.isDebug()));
                ++requiredArgumentIdx;
                value = nextArg;
            }

            value = value.trim();
            try
            {
                final @NonNull Argument.IParsedArgument<?> parsedArgument = argument.getParsedArgument(value, cap);

                // If the argument was already parsed before, update the value (in case of a repeatable argument,
                // the value is added to the list).
                final @Nullable Argument.IParsedArgument<?> result =
                    results.putIfAbsent(argument.getIdentifier(), parsedArgument);

                if (result != null)
                    result.updateValue(parsedArgument.getValue());
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalValueException(command, value, e, cap.isDebug());
            }
        }

        // If the help argument was specified, simply return null, because none of the other arguments matter.
        if (command.getHelpArgument() != null)
            if (results.containsKey(command.getHelpArgument().getIdentifier()))
                return null;

        for (final @NonNull Argument<?> argument : command.getArgumentManager().getArguments())
        {
            final boolean missing = !results.containsKey(argument.getIdentifier());

            // Ensure every required argument is present.
            if (argument.isRequired() && missing)
                throw new MissingArgumentException(command, argument, cap.isDebug());

            // Add default values for missing optional parameters.
            if (missing)
                results.put(argument.getIdentifier(), argument.getDefault());
        }
        return results;
    }

    /**
     * See {@link #getLastCommand(Command, int)}.
     */
    public @NonNull ParsedCommand getLastCommand()
        throws CommandNotFoundException
    {
        return getLastCommand(null, 0);
    }

    /**
     * Recursively gets the last command in the {@link #input}.
     * <p>
     * For example, in "<b><u>supercommand</u> <u>subcommand</u> -opt=val</b>" with <u>supercommand</u> and
     * <u>subcommand</u> being registered commands, it would return the {@link Command} object for <u>subcommand</u>.
     * <p>
     * Note that you can repeat a command more than once, e.g. "<b><u>supercommand</u> <u>subcommand</u>
     * <u>subcommand</u> <u>subcommand</u> -opt=val</b>" without any effects, it'll still return the {@link Command}
     * object for <u>subcommand</u>, just a little bit slower.
     *
     * @param command The latest {@link Command} that has been found so far.
     * @param idx     The index of the latest command in {@link #input}.
     * @return The last {@link Command} that can be parsed from the arguments in {@link #input}.
     *
     * @throws CommandNotFoundException If a command is found at an index that is not registered as subcommand of the
     *                                  previous command or if the subcommand has not registered the supercommand as
     *                                  such.
     */
    protected @NonNull ParsedCommand getLastCommand(final @Nullable Command command, final int idx)
        throws CommandNotFoundException
    {
        if (command == null)
        {
            if (idx != 0)
                throw new CommandNotFoundException(null, cap.isDebug());

            final @Nullable String commandName = input.size() > idx ? input.getArgs().get(idx).trim() : null;
            final Command baseCommand =
                cap.getCommand(commandName)
                   .orElseThrow(() -> new CommandNotFoundException(commandName, cap.isDebug()));

            // If the command has a super command, it cannot possible be right to be the first argument.
            if (baseCommand.getSuperCommand().isPresent())
                throw new CommandNotFoundException(baseCommand.getName(), cap.isDebug());

            return getLastCommand(baseCommand, 0);
        }

        if (command.getSubCommands().isEmpty())
            return new ParsedCommand(command, idx);

        final int nextIdx = idx + 1;
        final @Nullable String nextArg = input.size() > nextIdx ? input.getArgs().get(nextIdx).trim() : null;
        // If there's no argument available after the current one, we've reached the end of the arguments.
        // This means that the last command we found is the last argument (by definition), so return that.
        if (nextArg == null)
            return new ParsedCommand(command, idx);

        final @NonNull Optional<Command> subCommandOpt = command.getSubCommand(nextArg);
        if (!subCommandOpt.isPresent())
            return new ParsedCommand(command, idx);

        final @NonNull Command subCommand = subCommandOpt.get();

        if (!subCommand.getSuperCommand().isPresent() || subCommand.getSuperCommand().get() != command)
            // TODO: More specific exception.
            throw new CommandNotFoundException("super command of: " + subCommand.getName(), cap.isDebug());

        return new ParsedCommand(subCommand, nextIdx);
    }

    /**
     * Represents a parsed {@link Command}, disregarding any arguments.
     *
     * @author Pim
     */
    @Value
    static class ParsedCommand
    {
        Command command;
        Integer index;
    }
}
