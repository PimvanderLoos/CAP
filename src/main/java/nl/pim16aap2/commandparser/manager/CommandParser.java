package nl.pim16aap2.commandparser.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.argument.OptionalArgument;
import nl.pim16aap2.commandparser.argument.RepeatableArgument;
import nl.pim16aap2.commandparser.argument.RequiredArgument;
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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.pim16aap2.commandparser.argument.Argument.ParsedArgument;
import static nl.pim16aap2.commandparser.argument.RepeatableArgument.ParsedRepeatableArgument;

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
        System.out.println("Found parsedCommand: " + parsedCommand.getCommand().getName() +
                               " at idx: " + parsedCommand.getIndex());

        if (parsedCommand.getIndex() == (args.size() - 1) &&
            parsedCommand.getCommand().getRequiredArguments().size() > 0)
            return new CommandResult(commandSender, parsedCommand.getCommand());

        return new CommandResult(commandSender, parsedCommand.getCommand(),
                                 parseArguments(parsedCommand.getCommand(),
                                                parsedCommand.getIndex()));
    }

    private @NonNull Map<@NonNull String, ParsedArgument<?>> prepareParsing(final @NonNull Command command)
    {
        final @NonNull Map<@NonNull String, ParsedArgument<?>> results = new HashMap<>();

        for (final @NonNull RepeatableArgument<? extends List<?>, ?> repeatableArgument : command
            .getRepeatableArguments().values())
            results.put(repeatableArgument.getName(), new ParsedRepeatableArgument<>());

        return results;
    }

    private @NonNull ParsedArgument<?> parseArgument(final @NonNull Argument<?> argument, final @NonNull String value)
    {
        return new ParsedArgument<>(argument.getParser().parseArgument(value));
    }

    private @NonNull Map<@NonNull String, ParsedArgument<?>> parseArguments(final @NonNull Command command,
                                                                            final int idx)
        throws NonExistingArgumentException, MissingArgumentException
    {
        final @NonNull Map<@NonNull String, ParsedArgument<?>> results = prepareParsing(command);

        final int requiredArgumentIdx = 0;

        System.out.println();
        for (int pos = idx + 1; pos < args.size(); ++pos)
        {
            final String nextArg = args.get(pos);
            System.out.println("nextArg: " + nextArg);
            if (nextArg.charAt(0) == ARGUMENT_PREFIX)
            {
                final String[] parts = SEPARATOR_PATTERN.split(nextArg);
                final String argumentName = parts[0].substring(1);
                final @NonNull Argument<?> argument = command.getArgument(argumentName).orElseThrow(
                    () -> new NonExistingArgumentException(command, argumentName));

                if (argument instanceof RepeatableArgument) // TODO: The argument type should do this on its own.
                    parseRepeatableArgument(command, results, (RepeatableArgument<? extends List<?>, ?>) argument,
                                            argumentName, parts);
                else if (argument instanceof OptionalArgument)
                    parseOptionalArgument(command, results, (OptionalArgument<?>) argument, argumentName, parts);
            }
            else
            {
                // TODO: Better Exception.
                final @NonNull RequiredArgument<?> argument =
                    command.getRequiredArgumentFromIdx(requiredArgumentIdx)
                           .orElseThrow(() -> new MissingArgumentException(command,
                                                                           "Missing required argument at pos: " +
                                                                               requiredArgumentIdx));
                results.put(argument.getName(), parseArgument(argument, nextArg));
            }
        }

        for (final @NonNull OptionalArgument<?> optionalArgument : command.getOptionalArguments().values())
            results.putIfAbsent(optionalArgument.getName(),
                                new ParsedArgument<>(optionalArgument.getDefaultValue()));

        return results;
    }

    private void parseRepeatableArgument(final @NonNull Command command,
                                         final @NonNull Map<String, ParsedArgument<?>> results,
                                         final @NonNull RepeatableArgument<? extends List<?>, ?> argument,
                                         final @NonNull String argumentName, final @NonNull String[] parts)
        throws MissingArgumentException
    {
        // TODO: Reduce code duplication with optional argument
        final @Nullable String valStr = parts.length == 2 ? parts[1] : null;
        if (valStr == null)
            throw new MissingArgumentException(command, argumentName);

        final @Nullable ParsedRepeatableArgument<? extends List<?>, ?> parsed =
            (ParsedRepeatableArgument<? extends List<?>, ?>) results.get(argumentName);
        Objects.requireNonNull(parsed).addValue(argument, valStr);
    }

    private void parseOptionalArgument(final @NonNull Command command,
                                       final @NonNull Map<String, ParsedArgument<?>> results,
                                       final @NonNull OptionalArgument<?> argument, final @NonNull String argumentName,
                                       final @NonNull String[] parts)
        throws MissingArgumentException
    {
        if (argument.getFlag())
            results.put(argumentName, parseArgument(argument, argumentName));
        else
        {
            // TODO: Merge all subsequent parts? Or only split on the first?
            final @Nullable String valStr = parts.length == 2 ? parts[1] : null;
            if (valStr == null)
                throw new MissingArgumentException(command, argumentName);
            results.put(argumentName, parseArgument(argument, valStr));
        }
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
            if (idx != 0)
                throw new CommandNotFoundException("Command at index " + idx);

            final @Nullable String commandName = args.size() > idx ? args.get(idx) : null;
            final Command baseCommand = commandManager.getCommand(commandName)
                                                      .orElseThrow(() -> new CommandNotFoundException(commandName));

            // If the command has a super command, it cannot possible be right to be the first argument.
            if (baseCommand.getSuperCommand().isPresent())
                throw new CommandNotFoundException(baseCommand.getName());

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
            throw new CommandNotFoundException("super command of: " + subCommand.getName() + "");

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
