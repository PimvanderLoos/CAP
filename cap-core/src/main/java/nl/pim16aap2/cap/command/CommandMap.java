package nl.pim16aap2.cap.command;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CommandMap
{
    /**
     * The {@link CAP} instance managing this object.
     */
    private final @NonNull CAP cap;

    /**
     * The map containing all registered commands, with their names as key.
     */
    protected final @NonNull Map<@NonNull Locale, Map<@NonNull String, @NonNull Command>> commandMap = new LinkedHashMap<>();

    public CommandMap(final @NonNull CAP cap)
    {
        this.cap = cap;

        for (final @NonNull Locale locale : cap.getLocales())
            commandMap.put(locale, new HashMap<>());
    }

    /**
     * Gets a {@link Command} from its name.
     *
     * @param name   The name of the {@link Command}. See {@link Command#getName()}.
     * @param locale The {@link Locale} for which to get the {@link Command}.
     * @return The {@link Command#getName()} with the given name, if it is registered in the {@link CAP}.
     */
    public @NonNull Optional<Command> getCommand(@Nullable String name, @Nullable Locale locale)
    {
        locale = Util.valOrDefault(locale, cap.getDefaultLocale());
        if (name == null)
            return Optional.empty();

        name = cap.getMessage(name, locale);

        @NonNull Optional<Command> cmd = getFromMap(commandMap, locale, cap.getCommandNameCaseCheck(name));
        return cmd;
    }

    /**
     * Adds the provided command for every locale.
     *
     * @param command The {@link Command} to register.
     */
    public void addCommand(final @NonNull Command command)
    {
        for (final @NonNull Locale locale : cap.getLocales())
        {
            final @NonNull String name = cap.getCommandNameCaseCheck(cap.getMessage(command.getName(), locale));
            commandMap.get(locale).put(name, command);
        }
    }

    /**
     * Gets an entry from a localized map.
     *
     * @param map    The {@link Map} to retrieve the entry from.
     * @param locale The {@link Locale} to search in.
     * @param key    The key to search for in the locale.
     * @param <T>    The type of the value to search for.
     * @return The value if it could be found.
     */
    private @NonNull <T> Optional<T> getFromMap(
        final @NonNull Map<@NonNull Locale, Map<@NonNull String, @NonNull T>> map,
        final Locale locale, final @NonNull String key)
    {
        return Optional.ofNullable(map.get(locale)).map(entry -> entry.get(key));
    }

    public @NonNull Map<@NonNull String, @NonNull Command> get()
    {
        return get(null);
    }

    public @NonNull Map<@NonNull String, @NonNull Command> get(final @Nullable Locale locale)
    {
        final @NonNull Map<@NonNull String, @NonNull Command> map = commandMap
            .get(Util.valOrDefault(locale, cap.getDefaultLocale()));

        return map;
    }
}
