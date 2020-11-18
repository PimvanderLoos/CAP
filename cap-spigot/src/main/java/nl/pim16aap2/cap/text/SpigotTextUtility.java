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

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.pim16aap2.cap.text.decorator.ClickableTextCommandDecorator;
import nl.pim16aap2.cap.text.decorator.ISpigotTextDecorator;
import nl.pim16aap2.cap.text.decorator.ITextDecorator;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Represents a class with several utility methods for {@link Text} for the Spigot platform.
 *
 * @author Pim
 */
@UtilityClass
public class SpigotTextUtility
{
    /**
     * Creates a {@link BaseComponent} array from a {@link Text}.
     * <p>
     * This also takes care of any of the {@link ISpigotTextDecorator} to add functionality like clickable text.
     *
     * @param text The {@link Text} to convert.
     * @return The converted {@link Text}.
     */
    public @NonNull BaseComponent[] toBaseComponents(final @NonNull Text text)
    {
        if (text.getTextDecorators().size() == 0)
            return new TextComponent[]{new TextComponent(TextComponent.fromLegacyText(text.toString()))};

        final @NonNull ArrayList<BaseComponent> baseComponents = new ArrayList<>();
        int lastIdx = 0;
        for (final @NonNull ITextDecorator decorator : text.getTextDecorators())
        {
            if (lastIdx < decorator.getStart())
                baseComponents.add(toTextComponent(text, lastIdx, decorator.getStart()));

            final @NonNull TextComponent textComponent;
            if (decorator instanceof ISpigotTextDecorator)
                textComponent = ((ISpigotTextDecorator) decorator)
                    .getTextComponent(text.subsection(decorator.getStart(), decorator.getEnd()));
            else
                textComponent = toTextComponent(text, decorator.getStart(), decorator.getEnd());

            baseComponents.add(textComponent);
            lastIdx = decorator.getEnd();
        }

        if (lastIdx < text.getLength())
            baseComponents.add(toTextComponent(text, lastIdx, text.getLength()));

        final BaseComponent[] ret = new BaseComponent[baseComponents.size()];
        for (int idx = 0; idx < baseComponents.size(); ++idx)
            ret[idx] = baseComponents.get(idx);
        return ret;
    }

    /**
     * Directly converts a {@link Text} to a {@link TextComponent}, ignoring any {@link ITextDecorator}s.
     *
     * @param text The {@link Text} to convert literally.
     * @return The literally converted {@link Text}.
     */
    public @NonNull TextComponent toTextComponent(final @NonNull Text text)
    {
        return new TextComponent(TextComponent.fromLegacyText(text.toString()));
    }

    /**
     * Directly converts a subsection of a {@link Text} to a {@link TextComponent}, ignoring any {@link
     * ITextDecorator}s.
     *
     * @param text  The {@link Text} to convert literally.
     * @param start The start of the subsection of the {@link Text}.
     * @param end   The end of the subsection of the {@link Text}.
     * @return The literally converted {@link Text}.
     */
    public @NonNull TextComponent toTextComponent(final @NonNull Text text, final int start, final int end)
    {
        return new TextComponent(TextComponent.fromLegacyText(text.subsection(start, end).toString()));
    }

    /**
     * See {@link #addClickableCommandText(Text, String, TextType, String, String)}.
     */
    public @NonNull Text addClickableCommandText(final @NonNull Text text, final @NonNull String clickableText,
                                                 final @NonNull String command, final @NonNull TextType type)
    {
        return addClickableCommandText(text, clickableText, type, command, null);
    }

    /**
     * Adds a clickable piece of text that executes a command when clicked.
     *
     * @param text          The {@link Text} to append the clickable text to.
     * @param clickableText The string that can be clicked on.
     * @param type          The {@link TextType} of the clickable text.
     * @param command       The command to execute when clicked (e.g. "/say hello").
     * @param hoverMessage  The optional message to display when hovering over the the text. When not specified, no text
     *                      is shown.
     * @return The same {@link Text} instance.
     */
    public @NonNull Text addClickableCommandText(final @NonNull Text text, final @NonNull String clickableText,
                                                 final @NonNull TextType type, final @NonNull String command,
                                                 final @Nullable String hoverMessage)
    {
        final int clickableStart = text.getLength();
        final int clickableEnd = clickableStart + clickableText.length();

        text.add(clickableText, type);
        text.addDecorator(new ClickableTextCommandDecorator(clickableStart, clickableEnd, command, hoverMessage));

        return text;
    }

    /**
     * Creates a new {@link Text} with a clickable section.
     * <p>
     * See {@link #addClickableCommandText(Text, String, TextType, String, String)}.
     */
    public @NonNull Text addClickableCommandText(final @NonNull ColorScheme colorScheme,
                                                 final @NonNull String clickableText, final @NonNull TextType type,
                                                 final @NonNull String command, final @Nullable String hoverMessage)
    {
        return addClickableCommandText(new Text(colorScheme), clickableText, type, command, hoverMessage);
    }

    /**
     * Creates a new {@link Text} with a clickable section.
     * <p>
     * See {@link #addClickableCommandText(Text, String, TextType, String, String)}.
     */
    public @NonNull Text addClickableCommandText(final @NonNull ColorScheme colorScheme,
                                                 final @NonNull String clickableText, final @NonNull TextType type,
                                                 final @NonNull String command)
    {
        return addClickableCommandText(colorScheme, clickableText, type, command, null);
    }

    /**
     * See {@link #makeClickableCommand(Text, String, String)}.
     */
    public @NonNull Text makeClickableCommand(final @NonNull Text text, final @NonNull String command)
    {
        return makeClickableCommand(text, command, null);
    }

    /**
     * Makes the entire {@link Text} a clickable command.
     *
     * @param text         The {@link Text} to make execute a command when clicked.
     * @param command      The command to execute when clicked (e.g. "/say hello").
     * @param hoverMessage The optional message to display when hovering over the the text. When not specified, no text
     *                     is shown.
     * @return The same {@link Text} instance, now clickable.
     */
    public @NonNull Text makeClickableCommand(final @NonNull Text text, final @NonNull String command,
                                              final @Nullable String hoverMessage)
    {
        text.addDecorator(new ClickableTextCommandDecorator(0, text.getLength(), command, hoverMessage));
        return text;
    }
}
