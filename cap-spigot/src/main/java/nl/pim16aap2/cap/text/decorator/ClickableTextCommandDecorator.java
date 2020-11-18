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

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.pim16aap2.cap.text.SpigotTextUtility;
import nl.pim16aap2.cap.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an {@link ISpigotTextDecorator} for adding clickable text that executes commands when used.
 *
 * @author Pim
 */
@AllArgsConstructor
public class ClickableTextCommandDecorator implements ISpigotTextDecorator
{
    private int start;
    private int end;

    private final @NonNull String command;

    private final @Nullable String hoverMessage;

    @Override
    public int getStart()
    {
        return start;
    }

    @Override
    public int getEnd()
    {
        return end;
    }

    @Override
    @Contract("_ -> this")
    public @NonNull ITextDecorator setStart(int start)
    {
        this.start = start;
        return this;
    }

    @Override
    @Contract("_ -> this")
    public @NonNull ITextDecorator setEnd(int end)
    {
        this.end = end;
        return this;
    }

    @Override
    @Contract("_ -> this")
    public @NonNull ITextDecorator shift(int dist)
    {
        start += dist;
        end += dist;
        return this;
    }

    @Override
    public @NonNull ITextDecorator duplicate()
    {
        return new ClickableTextCommandDecorator(start, end, command, hoverMessage);
    }

    @Override
    public @NonNull TextComponent getTextComponent(final @NonNull Text text)
    {
        final @NonNull TextComponent textComponent = SpigotTextUtility.toTextComponent(text);

        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        if (hoverMessage != null)
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                       TextComponent.fromLegacyText(hoverMessage)));
        return textComponent;
    }
}
