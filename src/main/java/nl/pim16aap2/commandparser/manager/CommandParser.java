package nl.pim16aap2.commandparser.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.command.CommandResult;
import nl.pim16aap2.commandparser.commandsender.ICommandSender;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CommandParser
{
    private final @NonNull List<String> args;
    final @NonNull CommandManager commandManager;
    private final @NonNull ICommandSender commandSender;

    private static final char ARGUMENT_PREFIX = '-';

    // TODO: Allow space to be used as a separator
    private static final String SEPARATOR = "=";
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile(SEPARATOR);
    private static final Pattern NON_ESCAPED_QUOTATION_MARKS = Pattern.compile("(?<!\\\\)\"");

    public CommandParser(final @NonNull CommandManager commandManager, final @NonNull ICommandSender commandSender,
                         final @NonNull String[] args)
        throws EOFException
    {
        this.args = preprocess(args);
        this.commandSender = commandSender;
        this.commandManager = commandManager;
    }

    // TODO: Also take care of any "-key=val" or --longkey=val" or "-key val" here. Just store them separately.
    //       So the output becomes a list of strings for the positional arguments and a list of key/value pairs for
    //       free arguments.
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
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException

    {
        final @NonNull ParsedCommand parsedCommand = getLastCommand();
        if (parsedCommand.getIndex() == (args.size() - 1) &&
            parsedCommand.getCommand().getArgumentManager().getRequiredArguments().size() > 0)
            return new CommandResult(commandSender, parsedCommand.getCommand());

        return new CommandResult(commandSender, parsedCommand.getCommand(),
                                 parseArguments(parsedCommand.getCommand(),
                                                parsedCommand.getIndex()));
    }

    private void parseArgument(final @NonNull Argument<?> argument, final @NonNull String value,
                               final @NonNull Map<@NonNull String, Argument.IParsedArgument<?>> results)
    {
        final Argument.IParsedArgument<?> parsedArgument = argument.parseArgument(value);
        final Argument.IParsedArgument<?> result = results.putIfAbsent(argument.getName(), parsedArgument);
        if (result != null)
            result.updateValue(parsedArgument.getValue());
    }

    @Nullable
    private Map<@NonNull String, Argument.IParsedArgument<?>> parseArguments(final @NonNull Command command,
                                                                             final int idx)
        throws NonExistingArgumentException, MissingArgumentException
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
                                                                             commandManager.isDebug()));
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
                                                                             commandManager.isDebug()));
                ++requiredArgumentIdx;
                value = nextArg;
            }

            parseArgument(argument, value, results);
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
                throw new MissingArgumentException(command, argument, commandManager.isDebug());

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
                throw new CommandNotFoundException(null, commandManager.isDebug());

            final @Nullable String commandName = args.size() > idx ? args.get(idx) : null;
            final Command baseCommand =
                commandManager.getCommand(commandName)
                              .orElseThrow(() -> new CommandNotFoundException(commandName, commandManager.isDebug()));

            // If the command has a super command, it cannot possible be right to be the first argument.
            if (baseCommand.getSuperCommand().isPresent())
                throw new CommandNotFoundException(baseCommand.getName(), commandManager.isDebug());

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
            throw new CommandNotFoundException("super( command of: )" + subCommand.getName(), commandManager.isDebug());

        return new ParsedCommand(subCommand, nextIdx);
    }

    /**
     * Represents a parsed {@link Command}, disregarding any arguments.
     *
     * @author Pim
     */
    @AllArgsConstructor
    @Getter
    @Setter
    private static class ParsedCommand
    {
        Command command;
        Integer index;
    }
}
