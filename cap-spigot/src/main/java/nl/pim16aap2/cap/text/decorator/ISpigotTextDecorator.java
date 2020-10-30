package nl.pim16aap2.cap.text.decorator;

import lombok.NonNull;
import net.md_5.bungee.api.chat.TextComponent;
import nl.pim16aap2.cap.text.Text;

public interface ISpigotTextDecorator extends ITextDecorator
{
    @NonNull TextComponent getTextComponent(final @NonNull Text text);
}
