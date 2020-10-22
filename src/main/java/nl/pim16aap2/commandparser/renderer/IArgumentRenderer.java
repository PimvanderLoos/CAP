package nl.pim16aap2.commandparser.renderer;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.text.ColorScheme;
import nl.pim16aap2.commandparser.text.Text;

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
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @return The {@link Text} representing the {@link Argument}.
     */
    @NonNull Text render(final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument);

    /**
     * Renders the given {@link Argument} in long format.
     * <p>
     * Long format here means that it renders the argument itself as well as its summary.
     *
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @return The {@link Text} representing the {@link Argument}.
     */
    @NonNull Text renderLong(final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                             final @NonNull String summaryIndent);
}
