package nl.pim16aap2.cap.util;

import lombok.NonNull;
import lombok.Value;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;

/**
 * Represents a request made for tab-completion suggestions.
 *
 * @author Pim
 */
@Value
public class TabCompletionRequest
{
    /**
     * The {@link Command} for which the tab-completion suggestions were requested.
     */
    @NonNull Command command;
    /**
     * The {@link Argument} for which the tab-completion suggestions were requested.
     */
    @NonNull Argument<?> argument;
    /**
     * The {@link ICommandSender} that requested the tab-completion suggestions.
     */
    @NonNull ICommandSender commandSender;
    /**
     * The partial String to use as base for the tab-completion suggestions.
     */
    @NonNull String partial;

    /**
     * Whether this request was made asynchronously or not.
     * <p>
     * False = This request was made from the main thread.
     * <p>
     * True = This request was made asynchronously.
     */
    boolean async;

    /**
     * The {@link CAP} instance that issued this request.
     */
    @NonNull CAP cap;
}
