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

package nl.pim16aap2.cap.text.decorator;

import lombok.NonNull;
import net.md_5.bungee.api.chat.TextComponent;
import nl.pim16aap2.cap.text.Text;

/**
 * Represents an {@link ITextDecorator} for the Spigot platform.
 * <p>
 * This can be used to add functionality like clickable text (see {@link ClickableTextCommandDecorator}).
 *
 * @author Pim
 */
public interface ISpigotTextDecorator extends ITextDecorator
{
    /**
     * Creates a {@link TextComponent} from a {@link Text}.
     * <p>
     * Any of the special actions this {@link ITextDecorator} represents will be added to it as well.
     *
     * @param text The {@link Text} to convert into a {@link TextComponent}.
     * @return The new {@link TextComponent} created from the {@link Text} and this {@link ITextDecorator}'s special
     * stuff.
     */
    @NonNull TextComponent getTextComponent(final @NonNull Text text);
}
