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

package nl.pim16aap2.cap;

import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

class CommandRegistrator
{
    private static final @NonNull SimpleCommandMap simpleCommandMap;

    static
    {
        try
        {
            Field f = Bukkit.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            simpleCommandMap = (SimpleCommandMap) f.get(Bukkit.getServer().getPluginManager());
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            throw new RuntimeException("Failed to get SimpleCommandMap!", e);
        }
    }

    void registerCommands(final @NonNull JavaPlugin plugin, final @NonNull Map<String, Command> commands)
    {
        commands.forEach((commandName, command) -> registerCommand(plugin, commandName));
    }

    private void registerCommand(final @NonNull JavaPlugin plugin, final @NonNull String name)
    {
        simpleCommandMap.register(plugin.getName().toLowerCase(), new BukkitCommand(name));
    }

    private static class BukkitCommand extends org.bukkit.command.Command
    {
        public BukkitCommand(final @NonNull String name)
        {
            super(name);
        }

        @Override
        public boolean execute(final @NotNull CommandSender sender, final @NotNull String commandLabel,
                               final @NotNull String[] args)
        {
            throw new UnsupportedOperationException();
        }
    }
}
