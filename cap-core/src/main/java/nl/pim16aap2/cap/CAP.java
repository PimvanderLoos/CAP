package nl.pim16aap2.cap;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandparser.CommandParser;
import nl.pim16aap2.cap.commandparser.TabCompletionSuggester;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CAPException;
import nl.pim16aap2.cap.exception.ExceptionHandler;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.util.LocalizationSpecification;
import nl.pim16aap2.cap.util.Pair;
import nl.pim16aap2.cap.util.TabCompletionCache;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * The main class of this library. All commands within a single command system should be registered here.
 *
 * @author Pim
 */
public class CAP
{
    @Getter
    private final @NonNull Locale defaultLocale;

    private final @NonNull TabCompletionCache tabCompletionCache = new TabCompletionCache();

    /**
     * The map containing all registered commands, with their names as key.
     */
    protected final @NonNull Map<@NonNull Locale, Map<@NonNull String, @NonNull Command>> commandMap = new HashMap<>();

    /**
     * The map containing all registered top-level commands (i.e. commands without a supercommand of their own), with
     * their names as key.
     */
    protected final @NonNull Map<@NonNull Locale, Map<@NonNull String, @NonNull Command>> topLevelCommandMap = new HashMap<>();

    /**
     * The {@link DefaultHelpCommandRenderer} to use to render help messages.
     */
    @Getter
    protected final @NonNull DefaultHelpCommandRenderer helpCommandRenderer;

    /**
     * Whether or not to cache tab-completion suggestions using a {@link TabCompletionCache}.
     */
    @Getter
    @Setter
    protected boolean cacheTabCompletionSuggestions;

    /**
     * The {@link ExceptionHandler} that is used to handle CAP-related exceptions.
     * <p>
     * When null, all exceptions will be rethrown as {@link RuntimeException}s.
     */
    @Getter
    protected final @Nullable ExceptionHandler exceptionHandler;

    /**
     * The separator between a free argument's name and it's value. In the case of "--player=pim16aap2", this would be
     * "=". Default: " ".
     */
    @Getter
    protected final char separator;

    /**
     * Whether or not to enable debug mode. In debug mode, {@link CAPException}s will generate stacktraces, when it is
     * disabled, they won't (this is much faster).
     */
    @Getter
    @Setter
    protected boolean debug;

    /**
     * Whether to enable case sensitivity. Default: False.
     * <p>
     * When enabled, "/myCommand" will not match to "/mycommand". When disabled, this works fine.
     * <p>
     * Note that this only applies to command and argument names, not argument values.
     */
    @Getter
    protected final boolean caseSensitive;

    protected final @NonNull Locale[] locales;

    /**
     * The localization to use. When set to null, the raw values will be used instead.
     */
    protected final @Nullable LocalizationSpecification localizationSpecification;

    @Builder(toBuilder = true)
    protected CAP(final @Nullable DefaultHelpCommandRenderer helpCommandRenderer,
                  final @Nullable Boolean cacheTabCompletionSuggestions,
                  final @Nullable ExceptionHandler exceptionHandler, final @Nullable Character separator,
                  final boolean debug, final boolean caseSensitive,
                  final @Nullable LocalizationSpecification localizationSpecification)
    {
        this.helpCommandRenderer = Util.valOrDefault(helpCommandRenderer, DefaultHelpCommandRenderer.getDefault());
        this.cacheTabCompletionSuggestions = Util.valOrDefault(cacheTabCompletionSuggestions, true);
        this.exceptionHandler = exceptionHandler;
        this.separator = Util.valOrDefault(separator, ' ');
        this.debug = debug;
        this.caseSensitive = caseSensitive;
        this.localizationSpecification = localizationSpecification;

        if (localizationSpecification == null)
        {
            defaultLocale = Locale.ENGLISH;
            locales = new Locale[]{getDefaultLocale()};
//            System.out.println("0: Locale[0] " + locales[0]);
        }
        else
        {
            locales = localizationSpecification.getLocales();
            defaultLocale = localizationSpecification.getDefaultLocale();
//            System.out.println("1: Locale[0] " + locales[0]);
        }

        setupLocales();
    }

    /**
     * Instantiates the maps for all the registered {@link #locales}.
     */
    private void setupLocales()
    {
        for (final @NonNull Locale locale : locales)
        {
            commandMap.put(locale, new HashMap<>());
            topLevelCommandMap.put(locale, new HashMap<>());
        }
    }

    /**
     * Gets a new instance of this {@link CAP} using the default values.
     * <p>
     * Use {@link CAP#toBuilder()} if you wish to customize it.
     *
     * @return A new instance of this {@link CAP}.
     */
    public static @NonNull CAP getDefault()
    {
        return CAP.builder().build();
    }

    /**
     * Gets the translated message for a locale.
     * <p>
     * If {@link #localizationSpecification} is not available, the key itself is used.
     * <p>
     * If the value for the key cannot be found, it returns the key as well.
     *
     * @param key    The key.
     * @param locale The locale to use. Leave null to use the {@link #getDefaultLocale()}.
     * @return The localized message, if it can be found.
     */
    public @NonNull String getMessage(final @NonNull String key, @Nullable Locale locale)
    {
        if (localizationSpecification == null)
            return key;
        locale = Util.valOrDefault(locale, Locale.getDefault());
        final @NonNull ResourceBundle bundle = ResourceBundle
            .getBundle(localizationSpecification.getBaseName(), locale);
        return bundle.containsKey(key) ? bundle.getString(key) : key;
    }

    /**
     * Parses a string containing multiple arguments delimited by spaces.
     *
     * @param commandSender The {@link ICommandSender} that issued a command.
     * @param input         The string that may contain a set of commands and arguments.
     * @return The {@link CommandResult} if it could be constructed.
     */
    public @NonNull Optional<CommandResult> parseInput(final @NonNull ICommandSender commandSender,
                                                       final @NonNull String input)
    {
        try
        {
            return Optional.of(new CommandParser(this, commandSender, input, separator).parse());
        }
        catch (CAPException exception)
        {
            System.out.println("CAUGHT CAPEXCEPTION!!");
            if (exceptionHandler == null)
            {
                System.out.println("RETHROWING EXCEPTION AS RuntimeException!!");
                throw new RuntimeException(exception);
            }
            System.out.println("HANDLING Exception!!");
            exceptionHandler.handleException(commandSender, exception);
        }
        return Optional.empty();
    }

    /**
     * Registers a {@link Command} with this {@link CAP}.
     *
     * @param command The {@link Command} to register.
     * @return The current instance of this {@link CAP}.
     */
    public @NonNull CAP addCommand(final @NonNull Command command)
    {
        for (final @NonNull Locale locale : locales)
        {
//            System.out.println("Processing locale: " + locale);
            addCommand(command, locale);
        }
        return this;
    }

    /**
     * Adds the provided command for every locale.
     *
     * @param command The {@link Command} to register.
     */
    private void addCommand(final @NonNull Command command, final @NonNull Locale locale)
    {
        final @NonNull String name = getCommandNameCaseCheck(getMessage(command.getName(), locale));
        commandMap.get(locale).put(name, command);
        if (!command.getSuperCommand().isPresent())
            topLevelCommandMap.get(locale).put(name, command);
    }

    /**
     * Converts the name of a {@link Command} to lower case if needed (i.e. when {@link #caseSensitive} is disabled).
     * <p>
     * When it is not needed, the same value is returned.
     *
     * @param commandName The name of the {@link Command}.
     * @return The name of the command, made all lower case if needed.
     */
    @Contract("!null -> !null")
    protected String getCommandNameCaseCheck(final @Nullable String commandName)
    {
        if (commandName == null)
            return null;
        return caseSensitive ? commandName : commandName.toLowerCase();
    }

    /**
     * Gets a {@link Command} from its name for the {@link #getDefaultLocale()}.
     *
     * @param name The name of the {@link Command}. See {@link Command#getName()}.
     * @return The {@link Command#getName()} with the given name, if it is registered in the {@link CAP}.
     */
    public @NonNull Optional<Command> getCommand(final @Nullable String name)
    {
        return getCommand(name, getDefaultLocale());
    }

    /**
     * Gets a {@link Command} from its name.
     *
     * @param name   The name of the {@link Command}. See {@link Command#getName()}.
     * @param locale The {@link Locale} for which to get the {@link Command}.
     * @return The {@link Command#getName()} with the given name, if it is registered in the {@link CAP}.
     */
    public @NonNull Optional<Command> getCommand(final @Nullable String name, @Nullable Locale locale)
    {
        locale = Util.valOrDefault(locale, getDefaultLocale());
        if (name == null)
            return Optional.empty();

        return getFromMap(commandMap, Util.valOrDefault(locale, getDefaultLocale()), getCommandNameCaseCheck(name));
    }

    private @NonNull <T> Optional<T> getFromMap(
        final @NonNull Map<@NonNull Locale, Map<@NonNull String, @NonNull T>> map,
        final @NonNull Locale locale, final @NonNull String key)
    {
        return Optional.ofNullable(map.get(locale)).map(entry -> entry.get(key));
    }

    /**
     * Gets a super{@link Command} from its name (i.e. a {@link Command} without any supers of its own) for the {@link
     * #getDefaultLocale()}.
     *
     * @param name The name of the super{@link Command}. See {@link Command#getName()}.
     * @return The super{@link Command#getName()} with the given name, if it is registered in the {@link CAP}.
     */
    public @NonNull Optional<Command> getSuperCommand(final @Nullable String name)
    {
        return getSuperCommand(name, null);
    }

    /**
     * Gets a super{@link Command} from its name (i.e. a {@link Command} without any supers of its own).
     *
     * @param name   The name of the super{@link Command}. See {@link Command#getName()}.
     * @param locale The {@link Locale} for which to get the {@link Command}.
     * @return The super{@link Command#getName()} with the given name, if it is registered in the {@link CAP}.
     */
    public @NonNull Optional<Command> getSuperCommand(final @Nullable String name, final @Nullable Locale locale)
    {
        if (name == null)
            return Optional.empty();
        return getFromMap(topLevelCommandMap, Util.valOrDefault(locale, getDefaultLocale()),
                          getCommandNameCaseCheck(name));
    }

    /**
     * Gets a list of suggestions for tab complete based on the current set of arguments.
     *
     * @param commandSender The {@link ICommandSender} to get the suggestions for.
     * @param input         The current set of (potentially incomplete) input arguments.
     * @return The list of suggestions based on the current set of input arguments.
     */
    public @NonNull List<String> getTabCompleteOptions(final @NonNull ICommandSender commandSender,
                                                       final @NonNull String input)
    {
        final @NonNull TabCompletionSuggester suggester =
            new TabCompletionSuggester(this, commandSender, input, separator);
        final @NonNull Supplier<List<String>> supplier = () -> suggester.getTabCompleteOptions(false);

        if (!cacheTabCompletionSuggestions)
            return supplier.get();

        final @NonNull Pair<@NonNull String, @NonNull String> lastArgument = suggester.getLastArgumentData();

        return tabCompletionCache.getTabCompleteOptions(commandSender, suggester.getArgs(),
                                                        lastArgument.first + lastArgument.second, supplier,
                                                        suggester.isOpenEnded());
    }

    /**
     * Asynchronously gets a list of suggestions for tab complete based on the current set of arguments.
     *
     * @param commandSender The {@link ICommandSender} to get the suggestions for.
     * @param input         The current set of (potentially incomplete) input arguments.
     * @return The list of suggestions based on the current set of input arguments.
     */
    public @NonNull CompletableFuture<List<String>> getTabCompleteOptionsAsync(
        final @NonNull ICommandSender commandSender, final @NonNull String input)
    {
        final @NonNull TabCompletionSuggester suggester =
            new TabCompletionSuggester(this, commandSender, input, separator);
        final @NonNull Supplier<List<String>> supplier = () -> suggester.getTabCompleteOptions(true);

        if (!cacheTabCompletionSuggestions)
            return CompletableFuture.supplyAsync(supplier);

        final @NonNull Pair<@NonNull String, @NonNull String> lastArgument = suggester.getLastArgumentData();

        return tabCompletionCache.getTabCompleteOptionsAsync(commandSender, suggester.getArgs(),
                                                             lastArgument.first + lastArgument.second, supplier,
                                                             suggester.isOpenEnded());
    }

    /**
     * Gets all the top-level {@link Command}s for the provided {@link Locale} for the {@link #getDefaultLocale()}.
     *
     * @return All the top-level {@link Command}s
     */
    public @NonNull Map<@NonNull String, @NonNull Command> getTopLevelCommandMap()
    {
        return getTopLevelCommandMap(getDefaultLocale());
    }

    /**
     * Gets all the top-level {@link Command}s for the provided {@link Locale}.
     *
     * @param locale The {@link Locale} for which to get the {@link Command}.
     * @return All the top-level {@link Command}s
     */
    public @NonNull Map<@NonNull String, @NonNull Command> getTopLevelCommandMap(final @NonNull Locale locale)
    {
        final @NonNull Map<@NonNull String, @NonNull Command> map = topLevelCommandMap.get(locale);
        return map;
    }

    public static class CAPBuilder
    {
        private DefaultHelpCommandRenderer helpCommandRenderer;
        private Boolean cacheTabCompletionSuggestions;
        private ExceptionHandler exceptionHandler = ExceptionHandler.getDefault();
        private Character separator;
        private boolean debug;
        private boolean caseSensitive;
        private LocalizationSpecification localizationSpecification;
    }
}
