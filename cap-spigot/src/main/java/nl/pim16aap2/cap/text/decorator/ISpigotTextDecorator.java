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
