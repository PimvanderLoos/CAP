package nl.pim16aap2.cap;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandsender.AllowedCommandSenderType;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.commandsender.SpigotCommandSender;
import nl.pim16aap2.cap.commandsender.SpigotServerCommandSender;
import nl.pim16aap2.cap.exception.ExceptionHandler;
import nl.pim16aap2.cap.exception.NoPermissionException;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.renderer.SpigotHelpCommandRenderer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.SpigotColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextType;
import nl.pim16aap2.cap.util.LocalizationSpecification;
import nl.pim16aap2.cap.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represents a specialized class of {@link CAP} for the Spigot platform.
 *
 * @author Pim
 */
public class SpigotCAP extends CAP
{
    // Make sure this package was relocated properly.
    static
    {
        final String defaultPackage = new String(
            new byte[]{'n', 'l', '.', 'p', 'i', 'm', '1', '6', 'a', 'a', 'p', '2', '.', 'c', 'a', 'p'});

        if (CAP.class.getPackage().getName().equals(defaultPackage))
            throw new IllegalStateException("CAP was not relocated properly!");
    }

    @Getter
    private final @NonNull JavaPlugin plugin;

    @Getter
    @Setter
    private @NonNull ColorScheme colorScheme;

    @Builder(builderMethodName = "spigotCAPBuilder")
    protected SpigotCAP(final @Nullable DefaultHelpCommandRenderer helpCommandRenderer, final boolean debug,
                        final @NonNull JavaPlugin plugin, final @Nullable ColorScheme colorScheme,
                        final @Nullable ExceptionHandler exceptionHandler,
                        final @Nullable Character separator, final @Nullable Boolean cacheTabCompletionSuggestions,
                        final boolean caseSensitive,
                        final @Nullable LocalizationSpecification localizationSpecification)
    {
        super(Util.valOrDefault(helpCommandRenderer, SpigotHelpCommandRenderer.getDefault()),
              Util.valOrDefault(cacheTabCompletionSuggestions, true),
              Util.valOrDefault(exceptionHandler, ExceptionHandler.getDefault()),
              Util.valOrDefault(separator, ' '), debug, caseSensitive, localizationSpecification);

        this.plugin = plugin;
        this.colorScheme = Util.valOrDefault(colorScheme, generateColorScheme());
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), plugin);

        if (exceptionHandler == null && getExceptionHandler() != null)
            getExceptionHandler().setHandler(NoPermissionException.class, SpigotCAP::handleNoPermissionException);
    }

    /**
     * Gets a new instance of this {@link SpigotCAP} using the default values.
     *
     * @return A new instance of this {@link SpigotCAP}.
     */
    public static @NonNull SpigotCAP getDefault()
    {
        return SpigotCAP.spigotCAPBuilder().build();
    }

    public @NonNull Optional<CommandResult> parseInput(final @NonNull CommandSender sender,
                                                       final @NonNull String message)
    {
        final @NonNull ICommandSender commandSender = SpigotCommandSender.wrapCommandSender(sender, colorScheme);
        return parseInput(commandSender, message);
    }

    /**
     * Generates the default {@link ColorScheme}.
     *
     * @return The default {@link ColorScheme}.
     */
    protected @NonNull ColorScheme generateColorScheme()
    {
        return SpigotColorScheme
            .spigotColorSchemeBuilder()
            .addStyle(TextType.COMMAND, ChatColor.GOLD)
            .addStyle(TextType.OPTIONAL_PARAMETER, ChatColor.BLUE)
            .addStyle(TextType.OPTIONAL_PARAMETER_FLAG, ChatColor.LIGHT_PURPLE)
            .addStyle(TextType.OPTIONAL_PARAMETER_SEPARATOR, ChatColor.DARK_RED)
            .addStyle(TextType.OPTIONAL_PARAMETER_LABEL, ChatColor.DARK_AQUA)
            .addStyle(TextType.REQUIRED_PARAMETER, ChatColor.RED)
            .addStyle(TextType.REQUIRED_PARAMETER_FLAG, ChatColor.DARK_BLUE)
            .addStyle(TextType.REQUIRED_PARAMETER_SEPARATOR, ChatColor.BLACK)
            .addStyle(TextType.REQUIRED_PARAMETER_LABEL, ChatColor.WHITE)
            .addStyle(TextType.SUMMARY, ChatColor.AQUA)
            .addStyle(TextType.DESCRIPTION, ChatColor.GREEN)
            .addStyle(TextType.REGULAR_TEXT, ChatColor.DARK_PURPLE)
            .addStyle(TextType.HEADER, ChatColor.GREEN)
            .addStyle(TextType.SECTION, ChatColor.AQUA)
            .addStyle(TextType.FOOTER, ChatColor.DARK_RED)
            .addStyle(TextType.ERROR, ChatColor.DARK_RED, ChatColor.BOLD)
            .build();
    }

    /**
     * Checks if the {@link ICommandSender} is an instance of a {@link SpigotServerCommandSender} and they don't have
     * permission to a command, it means that that command was set to {@link AllowedCommandSenderType#PLAYER_ONLY}.
     * <p>
     * As such, we send a specialized message saying that the given command can only be used by {@link Player}s.
     *
     * @param commandSender The {@link ICommandSender} to check.
     * @param e             The Exception.
     */
    public static void handleNoPermissionException(final @NonNull ICommandSender commandSender,
                                                   final @NonNull NoPermissionException e)
    {
        if (commandSender instanceof SpigotServerCommandSender)
        {
            commandSender.sendMessage(
                new Text(commandSender.getColorScheme()).add("Only players can use this command!", TextType.ERROR));
        }
        else
            ExceptionHandler.handleCAPException(commandSender, e);
    }
}
