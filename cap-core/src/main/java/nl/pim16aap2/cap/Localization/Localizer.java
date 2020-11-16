package nl.pim16aap2.cap.Localization;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Represents
 */
public class Localizer
{
    /**
     * The default {@link Locale} to use when none is explicitly specified.
     * <p>
     * By default, this is the first provided locale.
     */
    @Getter
    private @Nullable Locale defaultLocale;

    @Getter
    private final @NonNull String baseName;
    /**
     * The array of registered locales.
     */
    @Getter
    private final @NonNull Locale[] locales;

    public Localizer(final @Nullable Locale defaultLocale, final @NonNull String baseName,
                     final @NonNull Locale... locales)
    {
        this.defaultLocale = Util.valOrDefault(defaultLocale, locales.length > 0 ? locales[0] : null);
        this.baseName = baseName;
        this.locales = locales.length == 0 ? new Locale[]{null} : locales;
    }

    public Localizer(final @NonNull String baseName, final @NonNull Locale... locales)
    {
        this(null, baseName, locales);
    }

    /**
     * Checks if a message can be localized in the provided {@link Locale}.
     *
     * @param key    The key of the message to check.
     * @param locale The {@link Locale} to check in.
     * @return True if the provided key can be localized.
     */
    public boolean isMessageLocalizable(final @Nullable String key, final @Nullable Locale locale)
    {
        if (key == null)
            return false;
        return ResourceBundle.getBundle(getBaseName(), Util.valOrDefault(locale, defaultLocale)).containsKey(key);
    }

    /**
     * Gets the translated message for a locale.
     * <p>
     * If the value for the key cannot be found, it returns the key as well.
     *
     * @param key    The key.
     * @param locale The locale to use. Leave null to use the {@link #getDefaultLocale()}.
     * @return The localized message, if it can be found.
     */
    public @NonNull String getMessage(final @NonNull String key, final @Nullable Locale locale)
    {
        final @NonNull ResourceBundle bundle = ResourceBundle.getBundle(getBaseName(),
                                                                        Util.valOrDefault(locale, defaultLocale));
        return bundle.containsKey(key) ? bundle.getString(key) : key;
    }

    /**
     * Gets the translated message for the locale of an {@link ICommandSender}. See {@link ICommandSender#getLocale()}.
     * <p>
     * If the value for the key cannot be found, it returns the key as well.
     *
     * @param key           The key.
     * @param commandSender The {@link ICommandSender} for which to get the {@link Locale}.
     * @return The localized message, if it can be found.
     */
    public @NonNull String getMessage(final @NonNull String key, final @NonNull ICommandSender commandSender)
    {
        return getMessage(key, commandSender.getLocale());
    }

    /**
     * The default {@link Locale} to use when none is explicitly specified.
     *
     * @param newDefaultLocale The new default {@link Locale} to use. Note that the selected {@link Locale} needs to be
     *                         registered in {@link #locales} on initialization!
     */
    public void setDefaultLocale(final @NonNull Locale newDefaultLocale)
    {
        // Make sure that the new default locale
        // is an already-registered one.
        for (final @NonNull Locale locale : locales)
            if (locale.equals(newDefaultLocale))
            {
                defaultLocale = newDefaultLocale;
                return;
            }

        throw new IllegalArgumentException(
            "Trying to register new default locale: " + newDefaultLocale.toString() +
                ", but this locale was not registered! Please register all desired locales on CAP initialization");
    }

    /**
     * Represents a disabled {@link Localizer}. All attempts to translate a message will just return the key.
     *
     * @author Pim
     */
    public static class Disabled extends Localizer
    {
        public Disabled()
        {
            super("DISABLED");
        }

        @Override
        public boolean isMessageLocalizable(final @Nullable String key, final @Nullable Locale locale)
        {
            return false;
        }

        @Override
        public @NonNull String getMessage(final @NonNull String key, final @Nullable Locale locale)
        {
            return key;
        }

        @Override
        public @NonNull String getMessage(final @NonNull String key, final @NonNull ICommandSender commandSender)
        {
            return key;
        }

        @Override
        public void setDefaultLocale(final @Nullable Locale newDefaultLocale)
        {
        }
    }
}
