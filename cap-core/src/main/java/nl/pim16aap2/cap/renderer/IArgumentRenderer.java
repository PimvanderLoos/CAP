package nl.pim16aap2.cap.renderer;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.localization.Localizer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents a renderer for arguments.
 *
 * @author Pim
 */
public interface IArgumentRenderer
{
    /**
     * Renders the given {@link Argument}.
     * <p>
     * The argument is rendered in 'short' format, so it renders just the argument; nothing else.
     *
     * @param localizer   The {@link Localizer} instance to use for localization.
     * @param locale      The {@link Locale} to use for rendering the {@link Argument}.
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @return The {@link Text} representing the {@link Argument}.
     */
    @NonNull Text render(final @NonNull Localizer localizer, final @Nullable Locale locale,
                         final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument);

    /**
     * Renders the given {@link Argument} in long format.
     * <p>
     * Long format here means that it renders the argument itself as well as its summary.
     *
     * @param localizer   The {@link Localizer} instance to use for localization.
     * @param locale      The {@link Locale} to use for rendering the {@link Argument}.
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @return The {@link Text} representing the {@link Argument}.
     */
    @NonNull Text renderLongFormat(final @NonNull Localizer localizer, final @Nullable Locale locale,
                                   final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                   final @NonNull String summaryIndent);
}
