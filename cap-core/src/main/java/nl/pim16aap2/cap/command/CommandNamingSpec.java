package nl.pim16aap2.cap.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents the specification of the name/summary/description etc for a {@link Command}.
 * <p>
 * See {@link CommandNamingSpec.Localized} and {@link CommandNamingSpec.RawStrings}.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class CommandNamingSpec
{
    /**
     * Whether the entries are localization entries or raw strings.
     */
    @Getter
    private final boolean localized;

    /**
     * The identifier for this named command. Mostly used for error logging etc.
     */
    @Getter
    private final @NonNull String identifier;

    /**
     * The name of the command.
     */
    private final @NonNull String name;

    /**
     * The description of the command. This is the longer description shown in the help menu for this command.
     */
    private @Nullable String description;

    /**
     * The summary of the command. This is the short description shown in the list of commands.
     */
    private @Nullable String summary;

    /**
     * The header of the command. This is text shown at the top of the help menu for this command.
     */
    private @Nullable String header;

    /**
     * The title of the section for the command-specific help menu.
     */
    private @Nullable String sectionTitle;

    /**
     * Verifies the entries. See {@link CAP#isMessageLocalizable(String, Locale)}.
     * <p>
     * Only applies to localized messages.
     *
     * @param cap The {@link CAP} instance to use for verification.
     */
    public void verify(final @NonNull CAP cap)
    {
        if (!localized)
            return;

        if (!cap.isMessageLocalizable(name, null))
            throw new IllegalStateException("Failed to find localization entry for key: \"" + name + "\"");
        description = cap.isMessageLocalizable(description, null) ? description : null;
        summary = cap.isMessageLocalizable(summary, null) ? summary : null;
        header = cap.isMessageLocalizable(header, null) ? header : null;
        sectionTitle = cap.isMessageLocalizable(sectionTitle, null) ? sectionTitle : null;
    }

    public @NonNull String getName(final @NonNull CAP cap, final @Nullable Locale locale)
    {
        return localized ? cap.getMessage(name, locale) : name;
    }

    public @Nullable String getDescription(final @NonNull CAP cap, final @Nullable Locale locale)
    {
        if (description == null)
            return null;
        return localized ? cap.getMessage(description, locale) : description;
    }

    public @Nullable String getSummary(final @NonNull CAP cap, final @Nullable Locale locale)
    {
        if (summary == null)
            return null;
        return localized ? cap.getMessage(summary, locale) : summary;
    }

    public @Nullable String getHeader(final @NonNull CAP cap, final @Nullable Locale locale)
    {
        if (header == null)
            return null;
        return localized ? cap.getMessage(header, locale) : header;
    }

    public @Nullable String getSectionTitle(final @NonNull CAP cap, final @Nullable Locale locale)
    {
        if (sectionTitle == null)
            return null;
        return localized ? cap.getMessage(sectionTitle, locale) : sectionTitle;
    }

    /**
     * Represents a naming specification for a {@link Command} using localized messages.
     *
     * @author Pim
     */
    public static class Localized extends CommandNamingSpec
    {
        /**
         * @param localizationKey The base localization key. When this value is specified on initialization, it will try
         *                        to find the values:
         *                        <p>
         *                        localizationKey + ".name", localizationKey + ".summary", localizationKey +
         *                        ".description", localizationKey + ".sectionTitle", and localizationKey + ".header" for
         *                        the respective variables.
         *                        <p>
         *                        A value for the name key is required, the others are optional.
         *                        <p>
         *                        See {@link CAP#isMessageLocalizable(String, Locale)}.
         */
        public Localized(final @NonNull String localizationKey)
        {
            super(true, localizationKey,
                  localizationKey + ".name",
                  localizationKey + ".description",
                  localizationKey + ".summary",
                  localizationKey + ".header",
                  localizationKey + ".sectionTitle");
        }
    }

    /**
     * Represents a naming specification for a {@link Command} using raw strings.
     *
     * @author Pim
     */
    public static class RawStrings extends CommandNamingSpec
    {
        /**
         * @param name         See {@link CommandNamingSpec#name}.
         * @param description  See {@link CommandNamingSpec#description}.
         * @param summary      See {@link CommandNamingSpec#summary}.
         * @param header       See {@link CommandNamingSpec#header}.
         * @param sectionTitle See {@link CommandNamingSpec#sectionTitle}.
         */
        @Builder
        public RawStrings(final @NonNull String name, final @Nullable String description,
                          final @Nullable String summary, final @Nullable String header,
                          final @Nullable String sectionTitle)
        {
            super(false, name, name, description, summary, header, sectionTitle);
        }
    }
}
