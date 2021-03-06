/*
 *  MIT License
 *
 * Copyright (c) 2020 Pim van der Loos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nl.pim16aap2.cap.exception;

import lombok.NonNull;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Represents a class that is responsible for handling {@link CAPException}s.
 * <p>
 * For example, when a command is not found, this class can inform the {@link ICommandSender} about this.
 *
 * @author Pim
 */
public class ExceptionHandler
{
    private final @NonNull Map<@NonNull Class<? extends CAPException>, @NonNull Handler<?>> handlers;

    protected ExceptionHandler(@NonNull Map<@NonNull Class<? extends CAPException>, @NonNull Handler<?>> handlers)
    {
        this.handlers = handlers;
    }

    /**
     * Updates the handler for a given {@link CAPException}.
     *
     * @param clazz   The {@link CAPException} class for which to update the handler.
     * @param handler The new handler.
     * @param <T>     The type of the {@link CAPException}.
     * @return This {@link ExceptionHandler} instance.
     */
    @Contract("_,_-> this")
    public <T extends CAPException> ExceptionHandler setHandler(final @NonNull Class<T> clazz,
                                                                final @NonNull Handler<T> handler)
    {
        handlers.put(clazz, handler);
        return this;
    }

    /**
     * Handles a {@link CAPException}.
     * <p>
     * If no handler is registered for this exception, a {@link RuntimeException} will be thrown with the provided
     * exception as cause.
     *
     * @param commandSender The {@link ICommandSender} for which to handle the exception.
     * @param exception     The {@link CAPException} to handle.
     * @param <T>           The type of the {@link CAPException}.
     */
    @SuppressWarnings("unchecked")
    public <T extends CAPException> void handleException(final @NonNull ICommandSender commandSender,
                                                         final @NonNull T exception)
    {
        final @NonNull Optional<@NonNull Handler<T>> handler = getHandler((Class<T>) exception.getClass());
        if (!handler.isPresent())
            throw new RuntimeException(exception);

        handler.get().accept(commandSender, exception);
    }

    /**
     * Gets the handler for a type of {@link CAPException}.
     *
     * @param clazz The {@link CAPException} class for which to update the handler.
     * @param <T>   The type of the {@link CAPException}.
     * @return The handler if it could be found.
     */
    @SuppressWarnings("unchecked")
    public <T extends CAPException> @NonNull Optional<@NonNull Handler<T>> getHandler(final @NonNull Class<T> clazz)
    {
        // This cast is safe because any handler that is added to the map was added using the
        // builder's/object's method that required a matching type for the key/value pair.
        final @Nullable @NonNull Handler<T> handler = (Handler<T>) handlers.get(clazz);

        return Optional.ofNullable(handler);
    }

    /**
     * Gets a new {@link ExceptionHandler} using the default exception handler for all {@link CAPException}s. (See
     * {@link #handleCAPException(ICommandSender, CAPException)}).
     * <p>
     * Use {@link #toBuilder()} if you wish to modify one or more of these handlers.
     *
     * @return A new {@link ExceptionHandler} using {@link #handleCAPException(ICommandSender, CAPException)} for all
     * {@link CAPException}s.
     */
    public static @NonNull ExceptionHandler getDefault()
    {
        return ExceptionHandler
            .builder()
            .handler(CAPException.class, ExceptionHandler::handleCAPException)
            .handler(CommandNotFoundException.class, ExceptionHandler::handleCAPException)
            .handler(ValidationFailureException.class, ExceptionHandler::handleCAPException)
            .handler(NoPermissionException.class, ExceptionHandler::handleCAPException)
            .handler(NonExistingArgumentException.class, ExceptionHandler::handleCAPException)
            .handler(MissingArgumentException.class, ExceptionHandler::handleCAPException)
            .handler(IllegalValueException.class, ExceptionHandler::handleCAPException)
            .handler(UnmatchedQuoteException.class, ExceptionHandler::handleCAPException)
            .handler(MissingValueException.class, ExceptionHandler::handleCAPException)
            .build();
    }

    protected static void sendError(final @NonNull ICommandSender commandSender, final @NonNull String message)
    {
        commandSender.sendMessage(new Text(commandSender.getColorScheme()).add(message, TextType.ERROR));
    }

    /**
     * The default exception handler. Sends {@link CAPException#getLocalizedMessage()} for the {@link ICommandSender}.
     *
     * @param commandSender The {@link ICommandSender}
     * @param ex            The {@link CAPException}.
     */
    public static void handleCAPException(final @NonNull ICommandSender commandSender, final @NonNull CAPException ex)
    {
        sendError(commandSender, ex.getLocalizedMessage());
    }

    // Delombok:
    public static ExceptionHandlerBuilder builder()
    {
        return new ExceptionHandlerBuilder();
    }

    public ExceptionHandlerBuilder toBuilder()
    {
        return new ExceptionHandlerBuilder()
            .handlers(handlers == null ? java.util.Collections.emptyMap() : handlers);
    }

    public static class ExceptionHandlerBuilder
    {
        private ArrayList<@NonNull Class<? extends CAPException>> handlers$key;
        private ArrayList<@NonNull Handler<?>> handlers$value;

        ExceptionHandlerBuilder()
        {
        }

        /**
         * Updates the handler for a given {@link CAPException}.
         *
         * @param handlerKey   The {@link CAPException} class for which to update the handler.
         * @param handlerValue The new handler.
         * @param <T>          The type of the {@link CAPException}.
         * @return This {@link ExceptionHandlerBuilder} instance.
         */
        @Contract("_, _-> this")
        public <T extends CAPException> ExceptionHandlerBuilder handler(final @NonNull Class<T> handlerKey,
                                                                        final @NonNull Handler<T> handlerValue)
        {
            if (handlers$key == null)
            {
                handlers$key = new ArrayList<>();
                handlers$value = new ArrayList<>();
            }
            handlers$key.add(handlerKey);
            handlers$value.add(handlerValue);
            return this;
        }

        /**
         * Copies all the keys and values from a set of handlers into the key/value lists.
         *
         * @param handlers The map whose values to copy.
         * @return This {@link ExceptionHandlerBuilder} instance.
         */
        @Contract("_-> this")
        private ExceptionHandlerBuilder handlers(
            Map<? extends @NonNull Class<? extends CAPException>, ? extends @NonNull Handler<?>> handlers)
        {
            if (handlers$key == null)
            {
                handlers$key = new ArrayList<>();
                handlers$value = new ArrayList<>();
            }
            for (final Map.Entry<? extends Class<? extends CAPException>, ? extends Handler<?>> $lombokEntry :
                handlers.entrySet())
            {
                handlers$key.add($lombokEntry.getKey());
                handlers$value.add($lombokEntry.getValue());
            }
            return this;
        }

        /**
         * Resets all the handlers that have been reset so far.
         *
         * @return This {@link ExceptionHandlerBuilder} instance.
         */
        @Contract("-> this")
        public ExceptionHandlerBuilder clearHandlers()
        {
            if (handlers$key != null)
            {
                handlers$key.clear();
                handlers$value.clear();
            }
            return this;
        }

        /**
         * Builds a new {@link ExceptionHandler} using the specified settings.
         *
         * @return This {@link ExceptionHandlerBuilder} instance.
         */
        public ExceptionHandler build()
        {
            Map<Class<? extends CAPException>, Handler<?>> handlers;
            switch (handlers$key == null ? 0 : handlers$key.size())
            {
                case 0:
                    handlers = new java.util.LinkedHashMap<>(0);
                    break;
                case 1:
                    handlers = new java.util.LinkedHashMap<>(1);
                    handlers.put(handlers$key.get(0), handlers$value.get(0));
                    break;
                default:
                    handlers = new java.util.LinkedHashMap<>(
                        handlers$key.size() < 1073741824 ?
                        1 + handlers$key.size() + (handlers$key.size() - 3) / 3 : Integer.MAX_VALUE);
                    for (int $i = 0; $i < handlers$key.size(); $i++)
                        handlers.put(handlers$key.get($i), handlers$value.get($i));
            }

            return new ExceptionHandler(handlers);
        }

        @Override
        public String toString()
        {
            return "ExceptionHandler.ExceptionHandlerBuilder(handlers$key=" + handlers$key + ", handlers$value=" +
                handlers$value + ")";
        }
    }

    /**
     * Typedef for the much longer type.
     *
     * @param <T> The type of the exception.
     */
    public interface Handler<T extends CAPException> extends @NonNull BiConsumer<@NonNull ICommandSender, @NonNull T>
    {
    }
}
