package nl.pim16aap2.cap.text;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.pim16aap2.cap.text.decorator.ISpigotTextDecorator;
import nl.pim16aap2.cap.text.decorator.ITextDecorator;

import java.util.ArrayList;

/**
 * Represents a class that can convert {@link Text} objects to {@link BaseComponent}s and add functionality using {@link
 * ISpigotTextDecorator}s.
 *
 * @author Pim
 */
@UtilityClass
public class SpigotTextParser
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
}
