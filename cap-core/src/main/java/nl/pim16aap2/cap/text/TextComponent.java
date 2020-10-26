package nl.pim16aap2.cap.text;

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

    /**
     * Creates a new text style without any 'off' value. This is useful when using {@link
     * ColorScheme.ColorSchemeBuilder#setDisableAll(String)} as that will set the default value.
     *
     * @param on The String to enable this Style.
     */
    public TextComponent(final @NonNull String on)
    {
        this(on, "");
    }
}
