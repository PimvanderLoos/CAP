package nl.pim16aap2.cap.commandparser;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.util.TabCompletionRequest;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Represents a class that can generate suggestions based on some input.
 *
 * @author Pim
 */
public class TabCompletionSuggester extends CommandParser
{
    /**
     * Keeps track of whether the commandline input is open ended.
     * <p>
     * This means that no partial values are present. This happens when
     * <p>
     * 1) No unmatched quotation marks are present (see {@link CommandLineInput#isCompleteQuotationMarks()} (if there
     * were, point 2 would be meaningless).
     * <p>
     * 2) The raw input (see {@link CommandLineInput#getRawInput()}) ends with a space. This indicates the previous
     * value was completed (provided point 1 is true).
     */
    @Getter
    protected final boolean openEnded;

    /**
     * The {@link Locale} to use. See {@link ICommandSender#getLocale()}.
     */
    @Getter
    protected final @Nullable Locale locale;

    /**
     * @param cap           The {@link CAP} instance that owns this object..
     * @param commandSender The {@link ICommandSender} that issued the command.
     * @param input         The string that may contain a set of commands and arguments.
     * @param separator     The separator between a free argument's flag and its value. E.g. '<i>=</i>' for the format
     *                      <i>'--player=pim16aap2'</i>.
     */
    public TabCompletionSuggester(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                  final @NonNull String input, final char separator)
    {
        super(cap, commandSender, new CommandLineInput(input), separator);
        openEnded = super.input.isCompleteQuotationMarks() && super.input.getRawInput().endsWith(" ");
        locale = commandSender.getLocale();
    }

    /**
     * Gets a list of suggestions for tab complete based on the current {@link #input}.
     *
     * @param async Whether or not this method was called asynchronously or not.
     * @return A list of tab completion suggestions.
     */
    public @NonNull List<@NonNull String> getTabCompleteOptions(final boolean async)
    {
        final @NonNull List<@NonNull String> ret = new ArrayList<>(0);
        final @NonNull String lastVal = input.getArgs().get(input.getArgs().size() - 1);
        try
        {
            final @NonNull CommandParser.ParsedCommand parsedCommand = getLastCommand();
            // The index after the last argument.
            // E.g., when no arguments are provided (i.e. the last value is a (sub)command), this will be 0.
            final int argumentIndex = input.size() - parsedCommand.getIndex() - 1;
            final int positionalArgCount = parsedCommand.getCommand().getArgumentManager()
                                                        .getPositionalArguments().size();

            // If the argumentIndex is 0 or 1, we also have to look at subcommands of the current command
            // Or siblings of the current command (if it has a super command).
            if (argumentIndex == 0)
            {
                // If the command is not open ended, it means that the user typing the input is still working on the
                // last (sub)command. As such, we return the names of all sibling commands.
                if (!openEnded)
                {
                    // If the command we found has a super command, its siblings are that super command's other
                    // subcommands. If it does not have a super command, it means that it is a top-level command,
                    // in which case, its siblings are the other top-level commands.
                    return parsedCommand.getCommand().getSuperCommand()
                                        .map(command -> getSubCommandSuggestions(command, lastVal))
                                        .orElseGet(() -> getTopLevelCommandSuggestions(lastVal));
                }

                // When the input is not open ended, we know the user is working on the next input, so we can get
                // all the current command's subcommands.
                ret.addAll(getSubCommandSuggestions(parsedCommand.getCommand(), ""));
            }
            else if (argumentIndex == 1 && !openEnded)
                ret.addAll(getSubCommandSuggestions(parsedCommand.getCommand(), lastVal));

            // If there are any positional arguments that haven't been processed yet, just add those.
            if (argumentIndex < positionalArgCount || (!openEnded && argumentIndex == positionalArgCount))
            {
                // First get the real index of the positional input argument.
                // When the command is open ended, we're looking at the current index.
                // When it isn't, we're still working on the previous one.
                final int positionalArgumentIndex = argumentIndex - (openEnded ? 0 : 1);
                ret.addAll(getPositionalArgumentSuggestions(parsedCommand.getCommand(),
                                                            positionalArgumentIndex, openEnded ? "" : lastVal, async));
            }
            else
                ret.addAll(getFreeArgumentSuggestions(parsedCommand.getCommand(), lastVal, async));

        }
        catch (CommandNotFoundException e)
        {
            return getTopLevelCommandSuggestions(lastVal);
        }
        return ret;
    }

    /**
     * Gets a list of names of all sub{@link Command}s of the provided {@link Command} that start with a specific
     * partial name.
     *
     * @param command     The {@link Command} for which to analyze all the sub{@link Command}s.
     * @param partialName The partial name that all subcommands must start with for them to be added to the list.
     * @return A list of names of sub{@link Command}s that start with the provided partial name.
     */
    protected @NonNull List<@NonNull String> getSubCommandSuggestions(final @NonNull Command command,
                                                                      final @NonNull String partialName)
    {
        final @NonNull List<@NonNull String> ret = new ArrayList<>();
        command.getSubCommands().forEach(subCommand ->
                                         {
                                             if (subCommand.getName(commandSender.getLocale()).startsWith(partialName))
                                                 ret.add(subCommand.getName(commandSender.getLocale()));
                                         });
        return ret;
    }

    /**
     * Gets the suggestions for all top-level {@link Command} (i.e. {@link Command}s that do not have their own
     * super{@link Command}) that start with a specific partial name.
     *
     * @param partialName The name a top-level {@link Command} has to start with for it to be added to the list.
     * @return A list containing the names of all top-level {@link Command}s that start with the provided partial name.
     */
    protected @NonNull List<@NonNull String> getTopLevelCommandSuggestions(final @NonNull String partialName)
    {
        final @NonNull List<@NonNull String> ret = new ArrayList<>();
        cap.getTopLevelCommandMap().forEach((name, cmd) ->
                                            {
                                                if (name.startsWith(partialName))
                                                    ret.add(name);
                                            });
        return ret;
    }


    /**
     * Gets the tab complete suggestions from {@link Argument#getTabCompleteFunction()}.
     *
     * @param command  The {@link Command} that owns the {@link Argument}.
     * @param argument The {@link Argument} that will be used to get the tab complete suggestions.
     * @param value    The current value to compare the results against.
     * @param prefix   The prefix to use for all suggestions.
     * @param async    Whether this request was made on the main thread.
     * @return The list of tab complete suggestions.
     */
    protected @NonNull List<String> getTabArgumentFunctionSuggestions(final @NonNull Command command,
                                                                      final @NonNull Argument<?> argument,
                                                                      final @NonNull String value,
                                                                      final @NonNull String prefix,
                                                                      final boolean async)
    {
        final List<String> options = new ArrayList<>(0);
        final @Nullable Argument.ITabCompleteFunction argumentValueCompletion = argument.getTabCompleteFunction();
        if (argumentValueCompletion == null)
            return options;

        argumentValueCompletion
            .apply(new TabCompletionRequest(command, argument, commandSender, value, async, cap))
            .forEach(entry ->
                     {
                         if (entry.startsWith(value))
                         {
                             if (entry.contains(" "))
                                 options.add(prefix + "\"" + entry + "\"");
                             else
                                 options.add(prefix + entry);
                         }
                     });

        return options;
    }

    /**
     * Gets the tab-completion suggestions for the positional {@link Argument} at the provided index.
     *
     * @param command                 The {@link Command} for which to get the positional {@link Argument}.
     * @param positionalArgumentIndex The index of the positional {@link Argument}.
     * @param partialValue            The name an {@link Argument} has to start with for it to be added to the list.
     * @param async                   Whether this request was made on the main thread.
     * @return A list of strings generated by {@link Argument#getTabCompleteFunction()} that start with the provided
     * partial match.
     */
    protected @NonNull List<@NonNull String> getPositionalArgumentSuggestions(final @NonNull Command command,
                                                                              final int positionalArgumentIndex,
                                                                              final @NonNull String partialValue,
                                                                              final boolean async)
    {
        final @NonNull Argument<?> positionalArgument =
            command.getArgumentManager().getPositionalArgumentAtIdx(positionalArgumentIndex)
                   .orElseThrow(() -> new RuntimeException(String.format(
                       "Failed to get positional argument at index %d for command %s with input: \"%s\"",
                       positionalArgumentIndex, command.getName(commandSender.getLocale()), input.getRawInput())));

        return getTabArgumentFunctionSuggestions(command, positionalArgument, partialValue, "", async);
    }

    /**
     * Gets all the tab-completion suggestions for a free {@link Argument}.
     * <p>
     * If the {@link Argument} hasn't been completed yet, the names of free {@link Argument}s will be suggested.
     * <p>
     * If the last {@link Argument} was completed (i.e. it has a separator),
     *
     * @param async Whether this request was made on the main thread.
     * @return A list of tab-completion suggestions for a free {@link Argument}.
     */
    protected @NonNull List<@NonNull String> getFreeArgumentSuggestions(final @NonNull Command command,
                                                                        final @NonNull String lastVal,
                                                                        final boolean async)
    {
        final @Nullable Argument<?> argument;
        String value;
        String prefix = "";

        if (spaceSeparated)
        {
            // Space-separated, so if the input is not open-ended, we know that we're still typing
            // the free argument's name, otherwise, we're starting the value.
            final @NonNull Optional<String> freeArgumentOpt = lStripArgumentPrefix(lastVal);
            if (freeArgumentOpt.isPresent())
            {
                if (!openEnded)
                    return getFreeArgumentNames(command, freeArgumentOpt.get());

                argument = command.getArgumentManager().getArgument(freeArgumentOpt.get().trim()).orElse(null);
                value = "";
            }
            // No argument prefixes, so the previous entry is the argument and the current one is the
            else
            {
                argument = lStripArgumentPrefix(input.getArgs().get(input.getArgs().size() - 2))
                    .flatMap(previousName -> command.getArgumentManager().getArgument(previousName.trim()))
                    .orElse(null);

                value = lastVal;

                if (argument != null && argument.isValuesLess())
                {
                    if (cap.isDebug())
                        System.out.printf("Argument %s is valueless, but a value is provided in input: \"%s\"!\n",
                                          argument.getIdentifier(), input.getRawInput());
                    return new ArrayList<>(0);
                }
            }
        }
        else
        {
            if (openEnded)
                return getFreeArgumentNames(command, "");

            final @NonNull String freeArgument = lStripArgumentPrefix(lastVal).orElseThrow(
                () -> new RuntimeException(
                    String.format("Could not find free argument from lastVal \"%s\" in input: \"%s\"",
                                  lastVal, input.getRawInput())));

            // If there is no separator, suggest some options to complete the name of the argument.
            // If the separator does exist, figure out which argument it is from the name and treat everything after the
            // separator as the value.
            final String[] parts = separatorPattern.split(freeArgument, 2);
            final String argumentName = parts[0].trim();
            value = parts.length == 2 ? parts[1].trim() : "";
            argument = command.getArgumentManager().getArgument(argumentName).orElse(null);

            // If the argument is present (and therefore completed) and valueless, there's nothing to complete.
            // However, it's not open-ended, so we're still working on the current argument.
            if (argument != null && argument.isValuesLess())
                return getFreeArgumentNames(command, freeArgument);

            // If the argument does not have a separator (otherwise there would be 2 parts)
            // Get all arguments starting with the current name.
            if (parts.length == 1)
                return getFreeArgumentNames(command, freeArgument);

            // If the argument exists and is complete, construct the prefix.
            if (argument != null)
            {
                if (argument.getShortName(cap, locale).equals(argumentName))
                    prefix = String.format("%c%s%s", ARGUMENT_PREFIX, argument.getShortName(cap, locale), separator);
                else if (argument.getLongName(cap, locale) == null)
                    prefix = "";
                else
                    prefix = String.format("%c%c%s%s",
                                           ARGUMENT_PREFIX, ARGUMENT_PREFIX, argument.getLongName(cap, locale),
                                           separator);
            }
        }

        if (argument == null)
            return new ArrayList<>(0);

        return getTabArgumentFunctionSuggestions(command, argument, value, prefix, async);
    }

    /**
     * Formats the {@link Argument#getShortName(CAP, Locale)} using the correct argument prefix and the provided
     * suffix.
     *
     * @param argument The argument whose short name to format.
     * @param suffix   The suffix to use. E.g. '=' for the format "-argument_shortName="
     * @return The formatted {@link Argument#getShortName(CAP, Locale)}.
     */
    protected @NonNull String getFormattedShortName(final @NonNull Argument<?> argument, final @NonNull String suffix)
    {
        return String.format("%c%s%s", ARGUMENT_PREFIX, argument.getShortName(cap, locale), suffix);
    }

    /**
     * Formats the {@link Argument#getLongName(CAP, Locale)} using the correct argument prefixes and the provided
     * suffix.
     *
     * @param argument The argument whose long name to format.
     * @param suffix   The suffix to use. E.g. '=' for the format "--argument_longName="
     * @return The formatted {@link Argument#getLongName(CAP, Locale)} if the {@link Argument#getLongName(CAP, Locale)}
     * exists, otherwise null.
     */
    protected @Nullable String getFormattedLongName(final @NonNull Argument<?> argument, final @NonNull String suffix)
    {
        return argument.getLongName(cap, locale) == null ? null :
               String.format("%c%s%s%s", ARGUMENT_PREFIX, ARGUMENT_PREFIX, argument.getLongName(cap, locale), suffix);
    }

    /**
     * Gets a list of {@link Argument#getShortName(CAP, Locale)}s and {@link Argument#getLongName(CAP, Locale)}s that
     * can be used to complete the current {@link #input}.
     *
     * @param command The {@link Command} for which to check the {@link Argument}s.
     * @param lastArg The last value in {@link #input} that will be used as a base for the auto suggestions. E.g. when
     *                supplied "a", it will suggest "admin" but it won't suggest "player" (provided "admin" is a
     *                registered {@link Argument} for the given {@link Command}.
     * @return The list of {@link Argument#getShortName(CAP, Locale)}s and {@link Argument#getLongName(CAP, Locale)}s
     * that can be used to complete the current {@link #input}.
     */
    protected @NonNull List<@NonNull String> getFreeArgumentNames(final @NonNull Command command,
                                                                  final @NonNull String lastArg)
    {
        final @NonNull List<@NonNull String> ret = new ArrayList<>(0);
        command.getArgumentManager().getArguments().forEach(
            argument ->
            {
                if (argument.isPositional())
                    return;


                final @NonNull String separator = argument.isValuesLess() ? "" : this.separator;

                final @NonNull String shortName = getFormattedShortName(argument, separator);
                final @Nullable String longName = getFormattedLongName(argument, separator);

                // Do not suggest valueless arguments that have already been provided.
                // Providing those twice doesn't do anything.
                if (argument.isValuesLess())
                {
                    // When checking if the value has already been provided, we prepaend and append some spaces.
                    // This makes sure that it doesn't match something like "--adventure for "-a"
                    if (input.getRawInput().contains(" " + shortName + " ") ||
                        (argument.getLongName(cap, locale) != null &&
                            input.getRawInput().contains(" " + longName + " ")))
                        return;
                }

                if (argument.getShortName(cap, locale).startsWith(lastArg))
                    ret.add(shortName);

                final @Nullable String localizedLongName = argument.getLongName(cap, locale);
                if (localizedLongName != null && longName != null && localizedLongName.startsWith(lastArg))
                    ret.add(longName);
            });
        return ret;
    }
}
