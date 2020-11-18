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

package nl.pim16aap2.cap.commandsender;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@AllArgsConstructor
public class SpigotServerCommandSender implements ISpigotCommandSender
{
    protected static final @NonNull ColorScheme EMPTY_COLOR_SCHEME = ColorScheme.builder().build();

    protected @Nullable Locale locale;

    @Override
    public void sendMessage(final @NonNull Text message)
    {
        System.out.println(message.toPlainString());
    }

    @Override
    public @NonNull ColorScheme getColorScheme()
    {
        return EMPTY_COLOR_SCHEME;
    }

    @Override
    public @Nullable Locale getLocale()
    {
        return locale;
    }

    @Override
    public int hashCode()
    {
        // A hashcode of 0 works fine, because we assume there to only be 1 server. Also, I'm pretty sure
        // command completion isn't supported anyway. Right?
        return 0;
    }

    @Override
    public String toString()
    {
        return "Server";
    }

    @Override
    public @NotNull CommandSender getCommandSender()
    {
        return Bukkit.getServer().getConsoleSender();
    }
}
