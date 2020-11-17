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
