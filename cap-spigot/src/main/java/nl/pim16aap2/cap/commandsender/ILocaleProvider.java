package nl.pim16aap2.cap.commandsender;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents an object that can provide a locale for a {@link CommandSender} when requested. This is later used for all
 * interaction with the {@link CommandSender}.
 *
 * @author Pim
 */
public interface ILocaleProvider
{
    /**
     * Provides the {@link Locale} for a {@link CommandSender}.
     * <p>
     * When the returned {@link Locale} is null, the default will be used instead.
     *
     * @param commandSender The {@link CommandSender} for which to get the {@link Locale}.
     * @return The {@link Locale} that will be used for a {@link CommandSender}.
     */
    @Nullable Locale getLocale(final @NonNull CommandSender commandSender);
}
