/*
 *  MIT License
 *
 * Copyright (c) 2020 Pim van der Loos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import nl.pim16aap2.cap.exception.MissingValueException;
import nl.pim16aap2.cap.exception.NoPermissionException;
import nl.pim16aap2.cap.exception.NonExistingArgumentException;
import nl.pim16aap2.cap.exception.UnmatchedQuoteException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.util.Pair;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
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
        throws UnmatchedQuoteException
    {
        this(cap, commandSender, new CommandLineInput(input), separator);

        if (!this.input.isCompleteQuotationMarks())
            throw new UnmatchedQuoteException(input, cap.getLocalizer()
                                                        .getMessage("error.exception.unmatchedQuotes", commandSender),
                                              cap.isDebug());
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
     *                                      IArgumentValidator#validate(CAP, ICommandSender, Argument, Object)}.
     * @throws IllegalValueException        If the specified value of an {@link Argument} is illegal.
     */
    // TODO: What's the difference between an IllegalValue and a ValidationFailure, exactly?
    public @NonNull CommandResult parse()
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException, NoPermissionException,
               ValidationFailureException, IllegalValueException, MissingValueException
    {
        final @NonNull ParsedCommand parsedCommand = getLastCommand();
        if (!parsedCommand.getCommand().hasPermission(commandSender))
        {
            final @NonNull String localizedMessage = cap.getLocalizer()
                                                        .getMessage("error.exception.noPermission", commandSender);
            throw new NoPermissionException(commandSender, parsedCommand.getCommand(), localizedMessage, cap.isDebug());
        }

        final int argCount = input.size() - 1 - parsedCommand.index;
        final int requiredArgCount = parsedCommand.getCommand().getArgumentManager().getRequiredArguments().size();
        if (argCount == 0 && requiredArgCount == 0)
            return new CommandResult(commandSender, parsedCommand.getCommand(),
                                     fillDefaultValues(parsedCommand.getCommand()));

        // If there aren't enough arguments to populate all required arguments,
        // We don't have to parse anything to know that it won't work. Instead, just
        // abort and complain about missing the required argument that was supposed to
        // come after the last provided one.
        if (argCount < requiredArgCount)
        {
            final @NonNull Argument<?> missingArgument = parsedCommand.getCommand().getArgumentManager()
                                                                      .getRequiredArguments().get(argCount);
            final @NonNull String argumentName = missingArgument.getShortName(cap.getLocalizer(),
                                                                              commandSender.getLocale());
            final @NonNull String localizedMessage =
                MessageFormat.format(cap.getLocalizer().getMessage("error.exception.missingValue",
                                                                   commandSender), argumentName);
            throw new MissingValueException(parsedCommand.getCommand(), missingArgument,
                                            localizedMessage, cap.isDebug());
        }

        return new CommandResult(commandSender, parsedCommand.getCommand(),
                                 parseArguments(parsedCommand.getCommand(),
                                                parsedCommand.getIndex()));
    }

    /**
     * Supplements a map of {@link Argument} values with any missing values for which a default value exists. See {@link
     * Argument#getDefault()}.
     *
     * @param command The {@link Command} for which to get all {@link Argument}s.
     * @param current The current map of {@link Argument} values.
     * @return The supplemented map of {@link Argument} values.
     */
    private @NonNull Map<@NonNull String, Argument.IParsedArgument<?>> fillDefaultValues(
        final @NonNull Command command,
        final @NonNull Map<@NonNull String, Argument.IParsedArgument<?>> current)
    {
        command.getArgumentManager().getArguments().forEach(
            argument -> current.putIfAbsent(argument.getIdentifier(), argument.getDefault()));
        return current;
    }

    /**
     * Gets a map of all available default values for all {@link Argument}s of the provided {@link Command}.
     * <p>
     * See {@link Argument#getDefault()}.
     *
     * @param command The {@link Command} to check the {@link Argument}s from.
     * @return The map with all available default values.
     */
    private @NonNull Map<@NonNull String, Argument.IParsedArgument<?>> fillDefaultValues(final @NonNull Command command)
    {
        return fillDefaultValues(command, new HashMap<>());
    }

    /**
     * Checks if a String is the name of a free argument. For this to be true, 2 requirements have to be met:
     * <p>
     * 1) The String has to start with at least 1 {@link #ARGUMENT_PREFIX}.
     * <p>
     * 2) The String (with up to two leading {@link #ARGUMENT_PREFIX}es stripped) has to be an {@link Argument}
     * registered with the {@link Command}.
     *
     * @param commandSender The {@link ICommandSender} for which to check the arguments.
     * @param command       The {@link Command} to check for a registered {@link Argument}.
     * @param name          The string that may contain the name of an {@link Argument}.
     * @return True if the provided name is the name of a free {@link Argument}.
     */
    protected static boolean isFreeArgumentName(final @NonNull ICommandSender commandSender,
                                                final @NonNull Command command, final @NonNull String name)
    {
        return lStripArgumentPrefix(name)
            .map(stripped -> command.getArgumentManager().getArgument(stripped, commandSender.getLocale()).isPresent())
            .orElse(false);
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
     *                                      IArgumentValidator#validate(CAP, ICommandSender, Argument, Object)}
     * @throws IllegalValueException        If the specified value of an {@link Argument} is illegal.
     */
    private @Nullable Map<@NonNull String, Argument.IParsedArgument<?>> parseArguments(final @NonNull Command command,
                                                                                       final int idx)
        throws NonExistingArgumentException, MissingArgumentException, ValidationFailureException,
               IllegalValueException, MissingValueException
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

                final @NonNull String argumentName = parts[0].substring(argNameStartIdx).trim();

                argument = command.getArgumentManager().getArgument(argumentName, commandSender)
                                  .orElseThrow(
                                      () ->
                                      {
                                          final @NonNull String localizedMessage =
                                              MessageFormat.format(cap.getLocalizer()
                                                                      .getMessage("error.exception.nonExistingArgument",
                                                                                  commandSender), argumentName);
                                          return new NonExistingArgumentException(command, argumentName,
                                                                                  localizedMessage, cap.isDebug());
                                      });

                if (argument.isValuesLess())
                    value = "";
                else
                {
                    // When the separator is a space, the value isn't stored in the second part of the
                    // current entry. The input is split on spaces and as such, it is stored in the next arg.
                    // So, we get the next arg and increment the current position by 1 to indicated we've
                    // just processed it.
                    final int nextPos = pos + 1;

                    @Nullable String foundValue;
                    if (spaceSeparated)
                    {
                        foundValue = nextPos >= input.size() ? null : input.getArgs().get(nextPos).trim();
                        // If the next value is another argument, then we can conclude that the
                        // value we found is not a value (it's an argument).
                        if (foundValue != null)
                            foundValue = isFreeArgumentName(commandSender, command, foundValue) ? null : foundValue;
                        pos += 1;
                    }
                    else
                    {
                        foundValue = parts.length == 1 ? null : parts[1].trim();
                        // If the value is empty, there was no value, so set it to null.
                        foundValue = (foundValue != null && foundValue.isEmpty()) ? null : foundValue;
                    }

                    // If no value is found, or if the value is another argument specification,
                    // we can conclude that this argument did not have a value.
                    if (foundValue == null)
                    {
                        final @NonNull String localizedMessage =
                            MessageFormat.format(cap.getLocalizer().getMessage("error.exception.missingValue",
                                                                               commandSender), argumentName);
                        throw new MissingValueException(command, argument, localizedMessage, cap.isDebug());
                    }
                    value = foundValue;
                }
            }
            else
            {
                final int currentRequiredArgumentIdx = requiredArgumentIdx;
                argument = command.getArgumentManager().getPositionalArgumentAtIdx(currentRequiredArgumentIdx)
                                  .orElseThrow(
                                      () ->
                                      {
                                          final @NonNull String localizedMessage =
                                              MessageFormat.format(cap.getLocalizer()
                                                                      .getMessage("error.exception.nonExistingArgument",
                                                                                  commandSender), nextArg);
                                          return new NonExistingArgumentException(command, nextArg,
                                                                                  localizedMessage, cap.isDebug());
                                      });
                ++requiredArgumentIdx;
                value = nextArg;
            }

            final @NonNull Argument.IParsedArgument<?> parsedArgument =
                argument.getParsedArgument(value.trim(), cap, commandSender);

            // If the argument was already parsed before, update the value (in case of a repeatable argument,
            // the value is added to the list).
            final @Nullable Argument.IParsedArgument<?> result =
                results.putIfAbsent(argument.getIdentifier(), parsedArgument);

            if (result != null)
                result.updateValue(parsedArgument.getValue());
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
            {
                final @NonNull String localizedMessage =
                    MessageFormat.format(cap.getLocalizer()
                                            .getMessage("error.exception.missingArgument", commandSender),
                                         argument.getLongName(cap.getLocalizer(), commandSender.getLocale()));
                throw new MissingArgumentException(command, argument, localizedMessage, cap.isDebug());
            }
        }

        fillDefaultValues(command, results);

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
     * <u>subCommand</u> being registered commands, it would return the {@link Command} object for <u>subCommand</u>.
     * <p>
     * Note that you can repeat a command more than once, e.g. "<b><u>supercommand</u> <u>subcommand</u>
     * <u>subCommand</u> <u>subCommand</u> -opt=val</b>" without any effects, it'll still return the {@link Command}
     * object for <u>subCommand</u>, just a little bit slower.
     *
     * @param command The latest {@link Command} that has been found so far.
     * @param idx     The index of the latest command in {@link #input}.
     * @return The last {@link Command} that can be parsed from the arguments in {@link #input}.
     *
     * @throws CommandNotFoundException If a command is found at an index that is not registered as subcommand of the
     *                                  previous command or if the subcommand has not registered the super command as
     *                                  such.
     */
    protected @NonNull ParsedCommand getLastCommand(final @Nullable Command command, final int idx)
        throws CommandNotFoundException
    {
        if (command == null)
        {
            if (idx != 0)
                throw new IllegalStateException(
                    String.format("Command was null at idx %d for input: \n%s", idx, input.toString()));

            final @Nullable String commandName = input.size() > idx ? input.getArgs().get(idx).trim() : null;

            // Gets the first argument in the arguments list.
            final Command baseCommand =
                cap.getTopLevelCommand(commandName, commandSender.getLocale())
                   .orElseThrow(() ->
                                {
                                    final @NonNull String localizedMessage =
                                        MessageFormat.format(cap.getLocalizer()
                                                                .getMessage("error.exception.commandNotFound",
                                                                            commandSender), commandName);
                                    return new CommandNotFoundException(Util.valOrDefault(commandName, "NULL"),
                                                                        localizedMessage, cap.isDebug());
                                });

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

        final @NonNull Optional<Command> subCommandOpt = command.getSubCommand(nextArg, commandSender.getLocale());
        if (!subCommandOpt.isPresent())
            return new ParsedCommand(command, idx);

        return getLastCommand(subCommandOpt.get(), nextIdx);
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
