package nl.pim16aap2.cap.text.decorator;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.pim16aap2.cap.text.SpigotTextParser;
import nl.pim16aap2.cap.text.Text;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class ClickableTextDecorator implements ISpigotTextDecorator
{
    private int start, end;

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
    public @NonNull ITextDecorator setStart(int start)
    {
        this.start = start;
        return this;
    }

    @Override
    public @NonNull ITextDecorator setEnd(int end)
    {
        this.end = end;
        return this;
    }

    @Override
    public @NonNull ITextDecorator shift(int dist)
    {
        start += dist;
        end += dist;
        return this;
    }

    @Override
    public @NonNull ITextDecorator duplicate()
    {
        return new ClickableTextDecorator(start, end, command, hoverMessage);
    }

    @Override
    public @NonNull TextComponent getTextComponent(final @NonNull Text text)
    {
        final @NonNull TextComponent textComponent = SpigotTextParser.toTextComponent(text);

        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        if (hoverMessage != null)
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                       TextComponent.fromLegacyText(hoverMessage)));
        return textComponent;
    }
}
