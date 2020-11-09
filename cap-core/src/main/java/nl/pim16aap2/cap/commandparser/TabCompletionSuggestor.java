package nl.pim16aap2.cap.commandparser;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.util.TabCompletionRequest;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TabCompletionSuggestor extends CommandParser
{
    /**
     * Constructs a new command parser.
     *
     * @param cap           The {@link CAP} instance that owns this object..
     * @param commandSender The {@link ICommandSender} that issued the command.
     * @param input         The string that may contain a set of commands and arguments.
     * @param separator     The separator between a free argument's flag and its value. E.g. '<i>=</i>' for the format
     *                      <i>'--player=pim16aap2'</i>.
     */
    public TabCompletionSuggestor(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                  final @NonNull String input, final char separator)
    {
        super(cap, commandSender, new CommandLineInput(input), separator);
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
     * Gets a list of {@link Argument#getName()} that can be used to complete the current {@link #input}.
     *
     * @param command The {@link Command} for which to check the {@link Argument}s.
     * @param lastArg The last value in {@link #input} that will be used as a base for the auto suggestions. E.g. when
     *                supplied "a", it will suggest "admin" but it won't suggest "player" (provided "admin" is a
     *                registered {@link Argument} for the given {@link Command}.
     * @return The list of {@link Argument#getName()} that can be used to complete the current {@link #input}.
     */
    protected @NonNull List<String> getTabCompleteArgumentNames(final @NonNull Command command,
                                                                final @NonNull String lastArg)
    {
        final List<String> ret = new ArrayList<>(0);
        command.getArgumentManager().getArguments().forEach(
            argument ->
            {
                if (argument.isPositional())
                    return;
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
     * Argument, String, String, boolean)} will be used for the first one (and an empty String as value, so every entry
     * will be accepted).
     * <p>
     * If the {@link Command} only has free {@link Argument}s, a list of those will be returned instead.
     * <p>
     * If the {@link Command} has any sub{@link Command}s, then those will be added to the result as well.
     *
     * @param command The {@link Command} for which to get the tab complete suggestions.
     * @return The list of tab complete suggestions.
     */
    protected @NonNull List<String> getTabCompleteWithoutValue(final @NonNull Command command, final boolean async)
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
            command.getArgumentManager().getPositionalArgumentAtIdx(0).ifPresent(
                arg -> ret.addAll(getTabCompleteFromArgumentFunction(command, arg, "", "", async)));

        return ret;
    }


    /**
     * Gets the tab complete suggestions from {@link Argument#getTabcompleteFunction()}.
     *
     * @param command  The {@link Command} that owns the {@link Argument}.
     * @param argument The {@link Argument} that will be used to get the tab complete suggestions.
     * @param value    The current value to compare the results against.
     * @param prefix   The prefix to use for all suggestions.
     * @param async    Whether this request was made on the main thread.
     * @return The list of tab complete suggestions.
     */
    protected @NonNull List<String> getTabCompleteFromArgumentFunction(final @NonNull Command command,
                                                                       final @NonNull Argument<?> argument,
                                                                       final @NonNull String value,
                                                                       final @NonNull String prefix,
                                                                       final boolean async)
    {
        final List<String> options = new ArrayList<>(0);
        final @Nullable Argument.ITabcompleteFunction argumentValueCompletion = argument.getTabcompleteFunction();
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
     * Gets the tab complete options for the {@link Argument}s of a {@link Command}.
     *
     * @param command The {@link Command} to get the tab complete options for.
     * @param async   Whether or not this method was called asynchronously.
     * @return The list of Strings suggested for the next parameter.
     */
    protected @NonNull List<String> getTabCompleteArguments(final @NonNull CommandParser.ParsedCommand command,
                                                            final boolean async)
    {
        final String lastVal = input.getArgs().get(input.size() - 1);

        // If the arguments are NOT separated by spaces, there's no point in looking at the before-last argument
        // So simply set the previous value to null and it'll be ignored.
        final @Nullable String previousVal =
            (spaceSeparated && input.size() > 2) ? input.getArgs().get(input.size() - 2) : null;

        // Get the before-last argument (which will be complete) if the previous value isn't null
        // which can be the case if it simply doesn't exist or if the arguments aren't space-separated.
        final @NonNull Optional<Argument<?>> previousArgument =
            previousVal == null ? Optional.empty() :
            lstripArgumentPrefix(previousVal).flatMap(previousName ->
                                                          command.getCommand().getArgumentManager()
                                                                 .getArgument(previousName.trim()));

        final @Nullable Argument<?> argument;
        String value;

        // The potential prefix of the suggestion. When the separator is a non-space, just providing the value of an
        // argument would override the argument on some platforms. As such "--player=pim" should not suggest "pim16aap2",
        // but rather "--player=pim16aap2".
        String prefix = "";

        if (previousArgument.isPresent() && !previousArgument.get().isValuesLess() &&
            !previousArgument.get().isPositional())
        {
            argument = previousArgument.orElse(null);
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

                final String[] parts = separatorPattern.split(freeArgument, 2);
                final String argumentName = parts[0].trim();
                value = parts.length == 2 ? parts[1].trim() : "";
                argument = command.getCommand().getArgumentManager().getArgument(argumentName).orElse(null);

                // If the argument is present (and therefore completed) and valueless, there's nothing to complete
                // As such, we can get all arguments.
                if (argument != null && argument.isValuesLess())
                    return getTabCompleteArgumentNames(command.getCommand(), "");

                // If the argument does not have a separator (otherwise there would be 2 parts)
                // Get all arguments starting with the current name.
                if (parts.length == 1)
                    return getTabCompleteArgumentNames(command.getCommand(), freeArgument);

                // If the argument exists and is complete, construct the prefix.
                if (argument != null)
                {
                    if (argument.getName().equals(argumentName))
                        prefix = String.format("%c%s%s", ARGUMENT_PREFIX, argument.getName(), separator);
                    else if (argument.getLongName() == null)
                        prefix = "";
                    else
                        prefix = String.format("%c%c%s%s",
                                               ARGUMENT_PREFIX, ARGUMENT_PREFIX, argument.getLongName(), separator);
                }
            }
            // If the argument is a positional argument (i.e. it doesn't start with the prefix and you don't have to
            // specify the name to use it), simply get the positional argument based on the position in the string.
            // As value, use the string that was found.
            else
            {
                // Subtract the command's index from the total size because the command and its supercommands don't
                // count towards positional indices.
                final int positionalArgumentIdx = input.size() - command.getIndex() - 2;
                argument = command.getCommand().getArgumentManager().getPositionalArgumentAtIdx(positionalArgumentIdx)
                                  .orElse(null);
                value = lastVal;
            }
        }

        if (argument == null)
            return new ArrayList<>(0);

        return getTabCompleteFromArgumentFunction(command.getCommand(), argument, value, prefix, async);
    }

    /**
     * Gets a list of suggestions for tab complete based on the current {@link #input}.
     *
     * @param async Whether or not this method was called asynchronously or not.
     * @return A list of tab completion suggestions.
     */
    public @NonNull List<String> getTabCompleteOptions(final boolean async)
    {
        try
        {
            final @NonNull CommandParser.ParsedCommand parsedCommand = getLastCommand();
            if (parsedCommand.getIndex() >= (input.size() - 1))
                return getTabCompleteWithoutValue(parsedCommand.getCommand(), async);

            final List<String> suggestions = new ArrayList<>();
            // Check if the found index is the before-last argument.
            // If it's further back than that, we know it cannot be subcommand,
            // and it has to be an argument.
            if (parsedCommand.getIndex() == (input.size() - 2) &&
                parsedCommand.getCommand().getSubCommands().size() > 0)
                suggestions.addAll(selectCommandsPartialMatch(input.getArgs().get(input.size() - 1),
                                                              parsedCommand.getCommand().getSubCommands()));

            if (parsedCommand.getCommand().getArgumentManager().getArguments().size() > 0)
                suggestions.addAll(getTabCompleteArguments(parsedCommand, async));

            return suggestions;
        }
        catch (CommandNotFoundException e)
        {
            return selectCommandsPartialMatch(input.getArgs().get(0),
                                              cap.getCommands().stream().filter(
                                                  command -> !command.getSuperCommand().isPresent())
                                                 .collect(Collectors.toList()));
        }
    }
}
