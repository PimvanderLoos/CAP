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
     * ColorScheme.ColorSchemeBuilder#setDisableAll(String)} as that will set the default value.
     *
     * @param on The String to enable this component.
     */
    public TextComponent(final @NonNull String on)
    {
        this(on, "");
    }
}
