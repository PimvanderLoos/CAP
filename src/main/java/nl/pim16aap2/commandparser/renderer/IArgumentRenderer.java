package nl.pim16aap2.commandparser.renderer;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
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
     * @param argument The {@link Argument} to render.
     * @return The {@link Text} representing the {@link Argument}.
     */
    @NonNull Text render(@NonNull Argument<?> argument);

    /**
     * Renders the given {@link Argument} in long format.
     * <p>
     * Long format here means that it renders the argument itself as well as its summary.
     *
     * @param argument The {@link Argument} to render.
     * @return The {@link Text} representing the {@link Argument}.
     */
    @NonNull Text renderLong(@NonNull Argument<?> argument, @NonNull String summaryIndent);
}
