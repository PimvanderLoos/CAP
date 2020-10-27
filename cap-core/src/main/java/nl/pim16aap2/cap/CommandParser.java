package nl.pim16aap2.cap;

import lombok.NonNull;
import lombok.Value;
import nl.pim16aap2.cap.argument.Argument;
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
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class CommandParser
{
    private final @NonNull List<String> args;
    final @NonNull nl.pim16aap2.cap.CAP cap;
    private final @NonNull ICommandSender commandSender;

    private static final char ARGUMENT_PREFIX = '-';
    private static final Pattern LEADING_PREFIX_PATTERN = Pattern.compile("^[-]{1,2}");

    // TODO: Allow space to be used as a separator
    private static final String SEPARATOR = "=";
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile(SEPARATOR);
    private static final Pattern NON_ESCAPED_QUOTATION_MARKS = Pattern.compile("(?<!\\\\)\"");

    public CommandParser(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                         final @NonNull String[] args)
        throws EOFException
    {
        this.args = preprocess(args);
        this.commandSender = commandSender;
        this.cap = cap;
    }

    private @NonNull List<String> selectCommandsPartialMatch(final @NonNull String partialName,
                                                             final @NonNull Collection<Command> commands)
    {
        final List<String> ret = new ArrayList<>(0);
        commands.forEach(
            subCommand ->
            {
                if (!commandSender.hasPermission(subCommand))
                    return;

                if (subCommand.getName().startsWith(partialName))
                    ret.add(subCommand.getName());
            });
        return ret;
    }

    private @NonNull List<String> getTabCompleteArgumentNames(final @NonNull Command command,
                                                              final @NonNull String lastArg)
    {
        final List<String> ret = new ArrayList<>(0);
        command.getArgumentManager().getArguments().forEach(
            argument ->
            {
                if (argument.getName().startsWith(lastArg))
                    ret.add(argument.getName());

                if (argument.getLongName() != null && argument.getLongName().startsWith(lastArg))
                    ret.add(argument.getLongName());
            });
        return ret;
    }

    /**
     * Gets the tab complete suggestions if there is no value at all. I.e. the last value in args is a (sub){@link
     * Command}.
     * <p>
     * If the {@link Command} has any positional {@link Argument}s, {@link #getTabCompleteFromArgumentFunction(Optional,
     * String)} will be used for the first one (and an empty String as value, so every entry will be accepted).
     * <p>
     * If the {@link Command} only has free {@link Argument}s, a list of those will be returned instead.
     * <p>
     * If the {@link Command} has any sub{@link Command}s, then those will be added to the result as well.
     *
     * @param command The {@link Command} for which to get the tab complete suggestions.
     * @return The list of tab complete suggestions.
     */
    private @NonNull List<String> getTabCompleteWithoutValue(final @NonNull Command command)
    {
        final List<String> ret = new ArrayList<>(0);

        command.getSubCommands().forEach(
            subCommand ->
            {
                if (commandSender.hasPermission(subCommand))
                    ret.add(subCommand.getName());
            });

        if (command.getArgumentManager().positionalArguments.isEmpty())
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
            return getTabCompleteFromArgumentFunction(command.getArgumentManager().getPositionalArgumentAtIdx(0), "");

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
     * @param argument The {@link Argument} that will be used to get the tab complete suggestions.
     * @param value    The current value to compare the results against.
     * @return The list of tab complete suggestions.
     */
    private @NonNull List<String> getTabCompleteFromArgumentFunction(final @NonNull Optional<Argument<?>> argument,
                                                                     final @NonNull String value)
    {
        final List<String> options = new ArrayList<>(0);
        final @Nullable Supplier<List<String>> argumentValueCompletion = argument.map(Argument::getTabcompleteFunction)
                                                                                 .orElse(null);
        if (argumentValueCompletion == null)
            return options;

        argumentValueCompletion.get().forEach(
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
    private @NonNull List<String> getTabCompleteArguments(final @NonNull ParsedCommand command)
    {
        final String lastVal = args.get(args.size() - 1);
        System.out.println("lastVal = " + lastVal);

        final @NonNull Optional<Argument<?>> argument;
        final String value;

        final @NonNull Optional<String> freeArgumentOpt = lstripArgumentPrefix(lastVal);
        // If the argument is a free argument (i.e. '--player=playerName'), try to complete the name of
        // the argument (in this case 'player') if there is no separator ('=' in this case) in the string.
        //
        // If the separator does exist, find the argument from the name and treat everything after the
        // separator as the value.
        if (freeArgumentOpt.isPresent())
        {
            final String freeArgument = freeArgumentOpt.get();
            if (!lastVal.contains(SEPARATOR))
                return getTabCompleteArgumentNames(command.getCommand(), freeArgument);

            final String[] parts = SEPARATOR_PATTERN.split(freeArgument, 2);
            final String argumentName = parts[0];
            value = parts[1];
            argument = command.getCommand().getArgumentManager().getArgument(argumentName);
        }
        // If the argument is a positional argument (i.e. it doesn't start with the prefix and you don't have to
        // specify the name to use it), simply get the positional argument based on the position in the string.
        // As value, use the string that was found.
        else
        {
            // Subtract the command's index from the total size because the command and its supercommands don't count
            // towards positional indices.
            final int positionalArgumentIdx = args.size() - command.index - 2;
            argument = command.getCommand().getArgumentManager().getPositionalArgumentAtIdx(positionalArgumentIdx);
            value = lastVal;
        }

        return getTabCompleteFromArgumentFunction(argument, value);
    }

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
    
    private @NonNull List<String> preprocess(final @NonNull String[] rawArgs)
        throws EOFException
    {
        final ArrayList<String> argsList = new ArrayList<>(rawArgs.length);

        @Nullable String arg = null;
        for (int idx = 0; idx < rawArgs.length; ++idx)
        {
            String entry = rawArgs[idx];
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

            if (arg != null && idx == (rawArgs.length - 1))
                throw new EOFException();
        }

        argsList.trimToSize();
        return argsList;
    }

    public @NonNull CommandResult parse()
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException, NoPermissionException,
               ValidationFailureException, IllegalValueException
    {
        final @NonNull ParsedCommand parsedCommand = getLastCommand();
        if (!commandSender.hasPermission(parsedCommand.getCommand()))
            throw new NoPermissionException(commandSender, parsedCommand.getCommand(), cap.isDebug());

        if (parsedCommand.getIndex() == (args.size() - 1) &&
            parsedCommand.getCommand().getArgumentManager().getRequiredArguments().size() > 0)
            return new CommandResult(commandSender, parsedCommand.getCommand());

        return new CommandResult(commandSender, parsedCommand.getCommand(),
                                 parseArguments(parsedCommand.getCommand(),
                                                parsedCommand.getIndex()));
    }

    private void parseArgument(final @NonNull Argument<?> argument, final @NonNull String value,
                               final @NonNull Map<@NonNull String, Argument.IParsedArgument<?>> results)
        throws ValidationFailureException
    {
        final Argument.IParsedArgument<?> parsedArgument = argument.getParsedArgument(value, cap);
        final Argument.IParsedArgument<?> result = results.putIfAbsent(argument.getName(), parsedArgument);
        if (result != null)
            result.updateValue(parsedArgument.getValue());
    }

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
            final @NonNull String value;
            if (nextArg.charAt(0) == ARGUMENT_PREFIX)
            {
                // If the second character is also an ARGUMENT_PREFIX, then the long name is used
                // If this is the case, then we simply have to start reading the argument name
                // 1 position later than for the short name (i.e. '-a' vs '--admin'.
                final int argNameStartIdx = nextArg.charAt(1) == ARGUMENT_PREFIX ? 2 : 1;
                final String[] parts = SEPARATOR_PATTERN.split(nextArg, 2);

                final @NonNull String argumentName = parts[0].substring(argNameStartIdx);
                argument = command.getArgumentManager().getArgument(argumentName)
                                  .orElseThrow(
                                      () -> new NonExistingArgumentException(command, argumentName,
                                                                             cap.isDebug()));
                value = argument.isValuesLess() ? "" : parts[1];
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

            try
            {
                parseArgument(argument, value, results);
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
    private @NonNull ParsedCommand getLastCommand()
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
    private @NonNull ParsedCommand getLastCommand(final @Nullable Command command, final int idx)
        throws CommandNotFoundException
    {
        if (command == null)
        {
            if (idx != 0) // TODO:
                throw new CommandNotFoundException(null, cap.isDebug());

            final @Nullable String commandName = args.size() > idx ? args.get(idx) : null;
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
        final @Nullable String nextArg = args.size() > nextIdx ? args.get(nextIdx) : null;
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
    private static class ParsedCommand
    {
        Command command;
        Integer index;
    }
}
