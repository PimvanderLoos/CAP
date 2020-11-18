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

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import nl.pim16aap2.cap.CAP;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Utility class for NamingSpec classes.
 *
 * @author Pim
 */
@UtilityClass
class NamingSpec
{
    /**
     * Gets the message from a value.
     * <p>
     * If localization is enabled, the translation of the value is returned.
     *
     * @param localizer The {@link Localizer} instance to use for localization (if localization is enabled).
     * @param locale    The {@link Locale} to search in if localization is enabled.
     * @param localized Whether or not to enabled localization.
     * @param val       The value to look up.
     * @return If localization is disabled, the value itself is returned. Otherwise we use {@link
     * Localizer#getMessage(String, Locale)}. If the input value is null, null is returned.
     */
    @Contract("_, _, _, !null -> !null")
    String getMessage(final @NonNull Localizer localizer, final @Nullable Locale locale,
                      final boolean localized, final @Nullable String val)
    {
        if (val == null)
            return null;
        return localized ? localizer.getMessage(val, locale) : val;
    }

    /**
     * Checks if a message can be localized.
     * <p>
     * See {@link Localizer#isMessageLocalizable(String, Locale)}.
     *
     * @param cap The {@link CAP} instance to use for localization.
     * @param val The key of the entry to check.
     * @return Null if the key is null or if no localization entry exists for it, otherwise the key itself.
     */
    @Nullable String checkMessage(final @NonNull CAP cap, final @Nullable String val)
    {
        return cap.getLocalizer().isMessageLocalizable(val, null) ? val : null;
    }
}
