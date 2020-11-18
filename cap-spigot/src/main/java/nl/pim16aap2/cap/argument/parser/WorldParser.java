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

package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * Represents an argument parser for {@link World} values.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorldParser extends ArgumentParser<World>
{
    @Override
    public @NonNull World parseArgument(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                        final @NonNull Argument<?> argument, final @NonNull String value)
        throws IllegalValueException
    {
        @Nullable World world;
        try
        {
            world = Bukkit.getWorld(UUID.fromString(value));
        }
        catch (IllegalArgumentException e)
        {
            world = Bukkit.getWorld(value);
        }

        if (world == null)
        {
            // TODO: Spigot-specific error messages.
            final @NonNull String localizedMessage =
                MessageFormat.format(cap.getLocalizer().getMessage("error.valueParser.integer", commandSender), value);
            throw new IllegalValueException(argument, value, localizedMessage, cap.isDebug());
        }

        return world;
    }

    public static WorldParser create()
    {
        return new WorldParser();
    }
}
