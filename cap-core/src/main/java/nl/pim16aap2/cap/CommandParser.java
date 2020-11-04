package nl.pim16aap2.cap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
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
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static nl.pim16aap2.cap.argument.Argument.ITabcompleteFunction;

class CommandParser
{
    private final @NonNull List<String> args;
    final @NonNull nl.pim16aap2.cap.CAP cap;
    private final @NonNull ICommandSender commandSender;

    private static final char ARGUMENT_PREFIX = '-';
    private static final Pattern LEADING_PREFIX_PATTERN = Pattern.compile("^[-]{1,2}");
    private static final Pattern NON_ESCAPED_QUOTATION_MARKS = Pattern.compile("(?<!\\\\)\"");

    @Getter
    protected boolean spaceSeparated;
    protected @NonNull String separator;
    protected @NonNull Pattern separatorPattern;

    /**
     * Constructs a new command parser.
     *
     * @param cap           The {@link CAP} instance that owns this object..
     * @param commandSender The {@link ICommandSender} that issued the command.
     * @param args          The list of arguments split on spaces (with preserved whitespace).
     * @param separator     The separator between a free argument's flag and its value. E.g. '<i>=</i>' for the format
     *                      <i>'--player=pim16aap2'</i>.
     * @throws EOFException If the command contains unmatched quotation marks. E.g. '<i>--player="pim 16aap2</i>'.
     */
    CommandParser(final @NonNull CAP cap, final @NonNull ICommandSender commandSender, final @NonNull List<String> args,
                  final @NonNull String separator)
        throws EOFException
    {
        this.separator = separator;
        separatorPattern = Pattern.compile(separator);
        spaceSeparated = separator.equals(" ");
        this.args = preprocess(args);
        this.commandSender = commandSender;
        this.cap = cap;
    }

    /**
     * Selects a list of {@link Command}s that start with a certain string from a superset of {@link Command}s.
     *
     * @param partialName The base of a {@link Command#getName()}. See {@link String#startsWith(String)}.
     * @param commands    The {@link Command}s to choose from.
     * @return The subset of {@link Command}s whose names start with the provided partial name.
     */
    protected @NonNull List<String> selectCommandsPartialMatch(final @NonNull String partialName,
                                                               final @NonNull Collection<Command> commands)
    {
        final List<String> ret = new ArrayList<>(0);
        commands.forEach(
            subCommand ->
            {
                if (!subCommand.hasPermission(commandSender))
                    return;

                if (subCommand.getName().startsWith(partialName))
                    ret.add(subCommand.getName());
            });
        return ret;
    }

    /**
     * Gets a list of {@link Argument#getName()} that can be used to complete the current {@link #args}.
     *
     * @param command The {@link Command} for which to check the {@link Argument}s.
     * @param lastArg The last value in {@link #args} that will be used as a base for the auto suggestions. E.g. when
     *                supplied "a", it will suggest "admin" but it won't suggest "player" (provided "admin" is a
     *                registered {@link Argument} for the given {@link Command}.
     * @return The list of {@link Argument#getName()} that can be used to complete the current {@link #args}.
     */
    protected @NonNull List<String> getTabCompleteArgumentNames(final @NonNull Command command,
                                                                final @NonNull String lastArg)
    {
        final List<String> ret = new ArrayList<>(0);
        command.getArgumentManager().getArguments().forEach(
            argument ->
            {
                final String separator = argument.isValuesLess() ? "" : this.separator;
                if (argument.getName().startsWith(lastArg))
                    ret.add(String.format("%c%s", ARGUMENT_PREFIX, argument.getName()) + separator);

                if (argument.getLongName() != null && argument.getLongName().startsWith(lastArg))
                    ret.add(
                        String.format("%c%s%s", ARGUMENT_PREFIX, ARGUMENT_PREFIX, argument.getLongName()) + separator);
            });
        return ret;
    }

    /**
     * Gets the tab complete suggestions if there is no value at all. I.e. the last value in args is a (sub){@link
     * Command}.
     * <p>
     * If the {@link Command} has any positional {@link Argument}s, {@link #getTabCompleteFromArgumentFunction(Command,
     * Optional, String)} will be used for the first one (and an empty String as value, so every entry will be
     * accepted).
     * <p>
     * If the {@link Command} only has free {@link Argument}s, a list of those will be returned instead.
     * <p>
     * If the {@link Command} has any sub{@link Command}s, then those will be added to the result as well.
     *
     * @param command The {@link Command} for which to get the tab complete suggestions.
     * @return The list of tab complete suggestions.
     */
    protected @NonNull List<String> getTabCompleteWithoutValue(final @NonNull Command command)
    {
        final List<String> ret = new ArrayList<>(0);

        command.getSubCommands().forEach(
            subCommand ->
            {
                if (subCommand.hasPermission(commandSender))
                    ret.add(subCommand.getName());
            });

        if (command.getArgumentManager().getPositionalArguments().isEmpty())
        {
            command.getArgumentManager().getArguments().forEach(
                argument ->
                {
                    ret.add(argument.getName());
                    if (argument.getLongName() != null)
                        ret.add(argument.getLongName());
                });
        }
        else
            return getTabCompleteFromArgumentFunction(command,
                                                      command.getArgumentManager().getPositionalArgumentAtIdx(0), "");

        return ret;
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
    static @NonNull Optional<String> lstripArgumentPrefix(final @NonNull String argument)
    {
        if (argument.length() > 0 && argument.charAt(0) != ARGUMENT_PREFIX)
            return Optional.empty();

        return Optional.of(LEADING_PREFIX_PATTERN.matcher(argument).replaceFirst(""));
    }

    /**
     * Gets the tab complete suggestions from {@link Argument#getTabcompleteFunction()}.
     *
     * @param command  The {@link Command} that owns the {@link Argument}.
     * @param argument The {@link Argument} that will be used to get the tab complete suggestions.
     * @param value    The current value to compare the results against.
     * @return The list of tab complete suggestions.
     */
    protected @NonNull List<String> getTabCompleteFromArgumentFunction(final @NonNull Command command,
                                                                       final @NonNull Optional<Argument<?>> argument,
                                                                       final @NonNull String value)
    {
        final List<String> options = new ArrayList<>(0);
        final @Nullable ITabcompleteFunction argumentValueCompletion = argument.map(Argument::getTabcompleteFunction)
                                                                               .orElse(null);
        if (argumentValueCompletion == null)
            return options;

        argumentValueCompletion.apply(command, argument.get()).forEach(
            entry ->
            {
                if (entry.startsWith(value))
                    options.add(entry);
            });

        return options;
    }

    /**
     * Gets the tab complete options for the {@link Argument}s of a {@link Command}.
     *
     * @param command The {@link Command} to get the tab complete options for.
     * @return The list of Strings suggested for the next parameter.
     */
    protected @NonNull List<String> getTabCompleteArguments(final @NonNull ParsedCommand command)
    {
        final String lastVal = args.get(args.size() - 1);

        // If the arguments are NOT separated by spaces, there's no point in looking at the before-last argument
        // So simply set the previous value to null and it'll be ignored.
        final @Nullable String previousVal = (spaceSeparated && args.size() > 2) ? args.get(args.size() - 2) : null;

        // Get the before-last argument (which will be complete) if the previous value isn't null
        // which can be the case if it simply doesn't exist or if the arguments aren't space-separated.
        final @NonNull Optional<Argument<?>> previousArgument =
            previousVal == null ? Optional.empty() :
            lstripArgumentPrefix(previousVal).flatMap(previousName ->
                                                          command.getCommand().getArgumentManager()
                                                                 .getArgument(previousName.trim()));

        final @NonNull Optional<Argument<?>> argument;
        String value;

        if (previousArgument.isPresent() && !previousArgument.get().isValuesLess() &&
            !previousArgument.get().isPositional())
        {
            argument = previousArgument;
            value = lastVal;
        }
        else
        {
            final @NonNull Optional<String> freeArgumentOpt = lstripArgumentPrefix(lastVal);
            // If the argument is a free argument (i.e. '--player=playerName'), try to complete the name of
            // the argument (in this case 'player') if there is no separator ('=' in this case) in the string.
            //
            // If the separator does exist, find the argument from the name and treat everything after the
            // separator as the value.
            if (freeArgumentOpt.isPresent())
            {
                final String freeArgument = freeArgumentOpt.get();
                if (!lastVal.contains(separator))
                    return getTabCompleteArgumentNames(command.getCommand(), freeArgument);

                final String[] parts = separatorPattern.split(freeArgument, 2);
                final String argumentName = parts[0];
                value = parts[1].trim();
                argument = command.getCommand().getArgumentManager().getArgument(argumentName);
            }
            // If the argument is a positional argument (i.e. it doesn't start with the prefix and you don't have to
            // specify the name to use it), simply get the positional argument based on the position in the string.
            // As value, use the string that was found.
            else
            {
                // Subtract the command's index from the total size because the command and its supercommands don't
                // count towards positional indices.
                final int positionalArgumentIdx = args.size() - command.index - 2;
                argument = command.getCommand().getArgumentManager().getPositionalArgumentAtIdx(positionalArgumentIdx);
                value = lastVal;
            }
        }

        return getTabCompleteFromArgumentFunction(command.command, argument, value);
    }

    /**
     * Gets a list of suggestions for tab complete based on the current {@link #args}.
     *
     * @return A list of tab completion suggestions.
     */
    public @NonNull List<String> getTabCompleteOptions()
    {
        try
        {
            final @NonNull ParsedCommand parsedCommand = getLastCommand();
            if (parsedCommand.getIndex() >= (args.size() - 1))
                return getTabCompleteWithoutValue(parsedCommand.getCommand());

            final List<String> suggestions = new ArrayList<>();
            // Check if the found index is the before-last argument.
            // If it's further back than that, we know it cannot be subcommand,
            // and it has to be an argument.
            if (parsedCommand.getIndex() == (args.size() - 2) &&
                parsedCommand.getCommand().getSubCommands().size() > 0)
                suggestions.addAll(selectCommandsPartialMatch(args.get(args.size() - 1),
                                                              parsedCommand.getCommand().getSubCommands()));

            if (parsedCommand.getCommand().getArgumentManager().getArguments().size() > 0)
                suggestions.addAll(getTabCompleteArguments(parsedCommand));

            return suggestions;
        }
        catch (CommandNotFoundException e)
        {
            return selectCommandsPartialMatch(args.get(0),
                                              cap.getCommands().stream().filter(
                                                  command -> !command.getSuperCommand().isPresent())
                                                 .collect(Collectors.toList()));
        }
    }

    /**
     * Preprocesses the arguments.
     * <p>
     * Any arguments that are split by spaces (and therefore in different entries) while they should be in a single
     * entry (because of quotation marks, e.g. 'name="my name"') will be merged into single entries.
     *
     * @param rawArgs The raw array of arguments split by spaces.
     * @return The list of preprocessed arguments.
     *
     * @throws EOFException When an unmatched quotation mark is encountered. E.g. 'name="my name'.
     */
    private @NonNull List<String> preprocess(final @NonNull List<String> rawArgs)
        throws EOFException
    {
        final ArrayList<@NonNull String> argsList = new ArrayList<>(rawArgs.size());

        // Represents a argument split by a spaces but inside brackets, e.g. '"my door"' should put 'my door' as a
        // single entry.
        @Nullable String arg = null;
        for (int idx = 0; idx < rawArgs.size(); ++idx)
        {
            String entry = rawArgs.get(idx);
            final Matcher matcher = NON_ESCAPED_QUOTATION_MARKS.matcher(entry);
            int count = 0;
            while (matcher.find())
                ++count;

            if (count > 0)
                entry = matcher.replaceAll("");

            // When there's an even number of (non-escaped) quotation marks, it means that there aren't any spaces
            // between them and as such, we can ignore them.
            if (count % 2 == 0)
            {
                // If arg is null, it means that we don't have to append the current block to another one
                // As such, we can add it to the list directly. Otherwise, we can add it to the arg and look for the
                // termination quotation mark in the next string.
                if (arg == null)
                    argsList.add(entry);
                else
                    arg += entry;
            }
            else
            {
                if (arg == null)
                    arg = entry;
                else
                {
                    argsList.add(arg + entry);
                    arg = null;
                }
            }

            if (arg != null && idx == (rawArgs.size() - 1))
                throw new EOFException();
        }

        argsList.trimToSize();
        return argsList;
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

        if (parsedCommand.getIndex() == (args.size() - 1) &&
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
     * @param idx     The index of the {@link Command} in {@link #args}. All values with a higher index than this will
     *                be processed as {@link Argument}s.
     * @return The map of {@link Argument.IParsedArgument}s resulting from parsing the input. Any missing optional
     * {@link Argument}s with default values will be assigned their default value. {@link Argument#getName()} is used
     * for the keys in the map.
     *
     * @throws NonExistingArgumentException If one of the specified arguments does not exist.
     * @throws MissingArgumentException     If a required argument was not specified.
     * @throws ValidationFailureException   If the value of an {@link Argument} could not be validated. See {@link
     *                                      IArgumentValidator#validate(Object)}.
     * @throws IllegalValueException        If the specified value of an {@link Argument} is illegal.
     */
    @Nullable
    private Map<@NonNull String, Argument.IParsedArgument<?>> parseArguments(final @NonNull Command command,
                                                                             final int idx)
        throws NonExistingArgumentException, MissingArgumentException, ValidationFailureException, IllegalValueException
    {
        final @NonNull Map<@NonNull String, Argument.IParsedArgument<?>> results = new HashMap<>();

        int requiredArgumentIdx = 0;
        for (int pos = idx + 1; pos < args.size(); ++pos)
        {
            final String nextArg = args.get(pos);
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
                        value = args.size() >= nextPos ? args.get(nextPos) : "";
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
                    results.putIfAbsent(argument.getName(), parsedArgument);

                if (result != null)
                    result.updateValue(parsedArgument.getValue());
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalValueException(command, value, e, cap.isDebug());
            }
        }

        // If the help argument was specified, simply return null, because none of the other argument matter.
        if (command.getHelpArgument() != null)
            if (results.containsKey(command.getHelpArgument().getName()) ||
                results.containsKey(command.getHelpArgument().getLongName()))
                return null;

        for (final @NonNull Argument<?> argument : command.getArgumentManager().getArguments())
        {
            final boolean missing = !results.containsKey(argument.getName());

            // Ensure every required argument is present.
            if (argument.isRequired() && missing)
                throw new MissingArgumentException(command, argument, cap.isDebug());

            // Add default values for missing optional parameters.
            if (missing)
                results.put(argument.getName(), argument.getDefault());
        }
        return results;
    }

    /**
     * See {@link #getLastCommand(Command, int)}.
     */
    protected @NonNull ParsedCommand getLastCommand()
        throws CommandNotFoundException
    {
        return getLastCommand(null, 0);
    }

    /**
     * Recursively gets the last command in the {@link #args}.
     * <p>
     * For example, in "<b><u>supercommand</u> <u>subcommand</u> -opt=val</b>" with <u>supercommand</u> and
     * <u>subcommand</u> being registered commands, it would return the {@link Command} object for <u>subcommand</u>.
     * <p>
     * Note that you can repeat a command more than once, e.g. "<b><u>supercommand</u> <u>subcommand</u>
     * <u>subcommand</u> <u>subcommand</u> -opt=val</b>" without any effects, it'll still return the {@link Command}
     * object for <u>subcommand</u>, just a little bit slower.
     *
     * @param command The latest {@link Command} that has been found so far.
     * @param idx     The index of the latest command in {@link #args}.
     * @return The last {@link Command} that can be parsed from the arguments in {@link #args}.
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

            final @Nullable String commandName = args.size() > idx ? args.get(idx).trim() : null;
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
        final @Nullable String nextArg = args.size() > nextIdx ? args.get(nextIdx).trim() : null;
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
