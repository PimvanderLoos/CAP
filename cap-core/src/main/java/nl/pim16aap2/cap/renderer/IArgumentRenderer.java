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

package nl.pim16aap2.cap.renderer;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.localization.Localizer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents a renderer for arguments.
 *
 * @author Pim
 */
public interface IArgumentRenderer
{
    /**
     * Renders the given {@link Argument}.
     * <p>
     * The argument is rendered in 'short' format, so it renders just the argument; nothing else.
     *
     * @param localizer   The {@link Localizer} instance to use for localization.
     * @param locale      The {@link Locale} to use for rendering the {@link Argument}.
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @return The {@link Text} representing the {@link Argument}.
     */
    @NonNull Text render(final @NonNull Localizer localizer, final @Nullable Locale locale,
                         final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument);

    /**
     * Renders the given {@link Argument} in long format.
     * <p>
     * Long format here means that it renders the argument itself as well as its summary.
     *
     * @param localizer   The {@link Localizer} instance to use for localization.
     * @param locale      The {@link Locale} to use for rendering the {@link Argument}.
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @return The {@link Text} representing the {@link Argument}.
     */
    @NonNull Text renderLongFormat(final @NonNull Localizer localizer, final @Nullable Locale locale,
                                   final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                   final @NonNull String summaryIndent);
}
