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

package nl.pim16aap2.cap.localization;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents the specification of the name/summary/description etc for a {@link Argument}.
 * <p>
 * See {@link ArgumentNamingSpec.Localized} and {@link ArgumentNamingSpec.RawStrings}.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ArgumentNamingSpec
{
    /**
     * Whether the entries are localization entries or raw strings.
     */
    @Getter
    protected final boolean localized;

    /**
     * The short name of the {@link Argument}.
     */
    private final @NonNull String shortName;

    /**
     * The (optional) long name of the {@link Argument}. For example, a help command usually has the short name '-h' and
     * the long name '--help'.
     */
    private @Nullable String longName;

    /**
     * A short summary to describe what this {@link Argument} does and/or how it is used.
     */
    private @Nullable String summary;

    /**
     * The label of the this {@link Argument}. For example, in the case of '[-p=player]', "player" would be the label.
     * <p>
     * This is also the name by which this Argument's result can be retrieved.
     */
    private @Nullable String label;

    /**
     * Verifies the entries. See {@link Localizer#isMessageLocalizable(String, Locale)}.
     * <p>
     * Only applies to localized messages.
     *
     * @param cap The {@link CAP} instance to use for verification.
     */
    public void verify(final @NonNull CAP cap)
    {
        if (!localized)
            return;

        if (!cap.getLocalizer().isMessageLocalizable(shortName, null))
            throw new IllegalStateException("Failed to find localization entry for key: \"" + shortName + "\"");

        longName = NamingSpec.checkMessage(cap, longName);
        summary = cap.getLocalizer().isMessageLocalizable(summary, null) ? summary : null;
        label = cap.getLocalizer().isMessageLocalizable(label, null) ? label : null;
    }

    public @NonNull String getShortName(final @NonNull Localizer localizer, final @Nullable Locale locale)
    {
        return NamingSpec.getMessage(localizer, locale, localized, shortName);
    }

    public @Nullable String getLongName(final @NonNull Localizer localizer, final @Nullable Locale locale)
    {
        return NamingSpec.getMessage(localizer, locale, localized, longName);
    }

    public @Nullable String getSummary(final @NonNull Localizer localizer, final @Nullable Locale locale)
    {
        return NamingSpec.getMessage(localizer, locale, localized, summary);
    }

    public @Nullable String getLabel(final @NonNull Localizer localizer, final @Nullable Locale locale)
    {
        System.out.println("Trying to get label for argument: " + shortName);
        return NamingSpec.getMessage(localizer, locale, localized, label);
    }

    /**
     * Represents a naming specification for a {@link Argument} using localized messages.
     *
     * @author Pim
     */
    public static class Localized extends ArgumentNamingSpec
    {
        /**
         * @param localizationKey The base localization key. When this value is specified on initialization, it will try
         *                        to find the values:
         *                        <p>
         *                        localizationKey + ".shortName", localizationKey + ".longName", localizationKey +
         *                        ".summary", and localizationKey + ".label", for the respective variables.
         *                        <p>
         *                        A value for the name key is required, the others are optional.
         *                        <p>
         *                        See {@link Localizer#isMessageLocalizable(String, Locale)}.
         */
        public Localized(final @NonNull String localizationKey)
        {
            super(true,
                  localizationKey + ".shortName",
                  localizationKey + ".longName",
                  localizationKey + ".summary",
                  localizationKey + ".label");
        }
    }

    /**
     * Represents a naming specification for a {@link Argument} using raw strings.
     *
     * @author Pim
     */
    public static class RawStrings extends ArgumentNamingSpec
    {
        /**
         * @param shortName See {@link ArgumentNamingSpec#shortName}.
         * @param longName  See {@link ArgumentNamingSpec#longName}.
         * @param summary   See {@link ArgumentNamingSpec#summary}.
         * @param label     See {@link ArgumentNamingSpec#label}.
         */
        @Builder
        public RawStrings(final @NonNull String shortName, final @Nullable String longName,
                          final @Nullable String summary, final @Nullable String label)
        {
            super(false, shortName, longName, summary, label);
        }
    }
}
