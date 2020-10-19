package nl.pim16aap2.commandparser.text;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class TextComponent
{
    private final @NonNull String on;
    private final @NonNull String off;

    /**
     * Creates a new text style.
     *
     * @param on  The String to enable this Style.
     * @param off The String to disable this style.
     */
    public TextComponent(final @NonNull String on, final @NonNull String off)
    {
        this.on = on;
        this.off = off;
    }
}
