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

package nl.pim16aap2.cap.text;

import lombok.Getter;
import lombok.NonNull;

/**
 * Represents a component in a piece of text. This can be a style such as "bold", or "green" or it can contain more
 * data.
 * <p>
 * Every component is stored by its enable and disable values. E.g. {@code on: <it>, off: </it>}.
 *
 * @author Pim
 */
@Getter
public class TextComponent
{
    /**
     * The String that is used to enable this component. E.g. {@code <it>}.
     */
    private final @NonNull String on;

    /**
     * The String that is used to disable this component. E.g. {@code </it>}.
     */
    private final @NonNull String off;

    /**
     * Creates a new text style.
     *
     * @param on  The String that is used to enable this component. E.g. {@code <it>}.
     * @param off The String that is used to disable this component. E.g. {@code </it>}.
     */
    public TextComponent(final @NonNull String on, final @NonNull String off)
    {
        this.on = on;
        this.off = off;
    }

    /**
     * Creates a new text component without any 'off' value. This is useful when using {@link
     * ColorScheme.ColorSchemeBuilder#setDefaultDisable(String)} as that will set the default value.
     *
     * @param on The String to enable this component.
     */
    public TextComponent(final @NonNull String on)
    {
        this(on, "");
    }
}
