package nl.pim16aap2.commandparser.manager;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.argument.OptionalArgument;
import nl.pim16aap2.commandparser.argument.RepeatableArgument;
import nl.pim16aap2.commandparser.argument.RequiredArgument;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.command.CommandResult;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;
import nl.pim16aap2.commandparser.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static nl.pim16aap2.commandparser.argument.Argument.ParsedArgument;
import static nl.pim16aap2.commandparser.argument.RepeatableArgument.ParsedRepeatableArgument;

class CommandParser
{
    private final @NonNull String[] args;
    final @NonNull CommandTree commandTree;

    private static final char ARGUMENT_PREFIX = '-';

    // TODO: Allow space to be used as a separator
    private static final String SEPARATOR = "=";
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile(SEPARATOR);

    public CommandParser(final @NonNull CommandTree commandTree, final @NonNull String[] args)
    {
        this.args = args;
        this.commandTree = commandTree;
    }

    public @NonNull CommandResult parse()
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException

    {
        int idx = 0;
        final String commandName = args[idx];
        final Command baseCommand = commandTree.getCommand(commandName)
                                               .orElseThrow(() -> new CommandNotFoundException(commandName));

        final @NonNull Pair<Command, Integer> parsedCommand = getTopCommand(baseCommand, idx);
        System.out.print("Found parsedCommand: " + parsedCommand.first.getName() + " at idx: " + parsedCommand.second);

        return new CommandResult(parsedCommand.first, parseArguments(parsedCommand.first, parsedCommand.second));
    }

    private @NonNull Map<@NonNull String, ParsedArgument<?>> prepareParsing(final @NonNull Command command)
    {
        final @NonNull Map<@NonNull String, ParsedArgument<?>> results = new HashMap<>();

        for (final @NonNull RepeatableArgument<? extends List<?>, ?> repeatableArgument : command
            .getRepeatableArguments().values())
            results.put(repeatableArgument.getName(), new ParsedRepeatableArgument<>());

        return results;
    }

    private @NonNull Map<@NonNull String, ParsedArgument<?>> parseArguments(final @NonNull Command command,
                                                                            final int idx)
        throws NonExistingArgumentException, MissingArgumentException
    {
        final @NonNull Map<@NonNull String, ParsedArgument<?>> results = prepareParsing(command);

        final int requiredArgumentIdx = 0;

        System.out.println();
        for (int pos = idx + 1; pos < args.length; ++pos)
        {
            final @Nullable String nextArg = args[pos];
            System.out.println("nextArg: " + nextArg);
            if (nextArg.charAt(0) == ARGUMENT_PREFIX)
            {
                final String[] parts = SEPARATOR_PATTERN.split(nextArg);
                final String argumentName = parts[0].substring(1);
                final @NonNull Argument<?> argument = command.getArgument(argumentName).orElseThrow(
                    () -> new NonExistingArgumentException(argumentName));

                if (argument instanceof OptionalArgument) // TODO: The argument type should do this on its own.
                    parseOptionalArgument(results, (OptionalArgument<?>) argument, argumentName, parts);
                else if (argument instanceof RepeatableArgument)
                    parseRepeatableArgument(results, (RepeatableArgument<? extends List<?>, ?>) argument, argumentName,
                                            parts);
            }
            else
            {
                // TODO: Better Exception.
                final @NonNull RequiredArgument<?> argument =
                    command.getRequiredArgumentFromIdx(requiredArgumentIdx)
                           .orElseThrow(() -> new MissingArgumentException("Missing required argument at pos: " +
                                                                               requiredArgumentIdx));
                results.put(argument.getName(), new ParsedArgument<>(argument.getParser().apply(nextArg)));
            }
        }

        for (final @NonNull OptionalArgument<?> optionalArgument : command.getOptionalArguments().values())
            results.putIfAbsent(optionalArgument.getName(),
                                new ParsedArgument<>(optionalArgument.getDefaultValue()));

        return results;
    }

    private void parseRepeatableArgument(final @NonNull Map<String, ParsedArgument<?>> results,
                                         final @NonNull RepeatableArgument<? extends List<?>, ?> argument,
                                         final @NonNull String argumentName, final @NonNull String[] parts)
        throws MissingArgumentException
    {
        // TODO: Reduce code duplication with optional argument
        final @Nullable String valStr = parts.length == 2 ? parts[1] : null;
        if (valStr == null)
            throw new MissingArgumentException(argumentName);

        final @Nullable ParsedRepeatableArgument<? extends List<?>, ?> parsed =
            (ParsedRepeatableArgument<? extends List<?>, ?>) results.get(argumentName);
        Objects.requireNonNull(parsed).addValue(argument, valStr);
    }

    private void parseOptionalArgument(final @NonNull Map<String, ParsedArgument<?>> results,
                                       final @NonNull OptionalArgument<?> argument,
                                       final @NonNull String argumentName, final @NonNull String[] parts)
        throws MissingArgumentException
    {
        if (argument.getFlag())
            results.put(argumentName, new ParsedArgument<>(argument.getParser().apply(argumentName)));
        else
        {
            // TODO: Merge all subsequent parts? Or only split on the first?
            final @Nullable String valStr = parts.length == 2 ? parts[1] : null;
            if (valStr == null)
                throw new MissingArgumentException(argumentName);

            results.put(argumentName, new ParsedArgument<>(argument.getParser().apply(valStr)));
        }
    }

    private @NonNull Pair<Command, Integer> getTopCommand(final @NonNull Command command, final int idx)
    {
        if (command.getSubCommands().isEmpty())
            return new Pair<>(command, idx);

        final int nextIdx = idx + 1;
        final @Nullable String nextArg = args.length > nextIdx ? args[nextIdx] : null;
        if (nextArg == null)
            return new Pair<>(command, idx);

        return command.getSubCommand(nextArg)
                      .map(subCommand -> getTopCommand(subCommand, nextIdx))
                      .orElse(new Pair<>(command, idx));
    }
}
