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

package nl.pim16aap2.cap.command;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.util.LocalizedMap;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * The {@link LocalizedMap} for {@link Command}s.
 *
 * @author Pim
 */
public class CommandMap extends LocalizedMap<Command>
{
    protected final @NonNull CAP cap;

    public CommandMap(final @NonNull CAP cap, final int initialCapacity)
    {
        super(cap.getLocalizer(), initialCapacity);
        this.cap = cap;
    }

    public CommandMap(final @NonNull CAP cap)
    {
        super(cap.getLocalizer());
        this.cap = cap;
    }

    /**
     * Gets a {@link Command} from its name.
     *
     * @param nameKey The key for the name of the {@link Command}. See {@link Command#getIdentifier()}.
     * @param locale  The {@link Locale} for which to get the {@link Command}.
     * @return The {@link Command)} with the given name, if it is registered here.
     */
    public @NonNull Optional<Command> getCommand(@Nullable String nameKey, @Nullable Locale locale)
    {
        return getEntry(nameKey, locale, cap::getCommandNameCaseCheck);
    }

    /**
     * Adds the provided command for every locale.
     *
     * @param command The {@link Command} to register.
     */
    public void addCommand(final @NonNull Command command)
    {
        addEntry((IGNOREME, locale) -> command.getName(locale), command, cap::getCommandNameCaseCheck);
    }
}
