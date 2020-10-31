package nl.pim16aap2.cap;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.commandsender.SpigotCommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.exception.MissingArgumentException;
import nl.pim16aap2.cap.exception.NoPermissionException;
import nl.pim16aap2.cap.exception.NonExistingArgumentException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.renderer.SpigotHelpCommandRenderer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.SpigotColorScheme;
import nl.pim16aap2.cap.text.TextType;
import nl.pim16aap2.cap.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;

/**
 * Represents a specialized class of {@link CAP} for the Spigot platform.
 *
 * @author Pim
 */
public class SpigotCAP extends CAP
{
    @Getter
    private final @NonNull JavaPlugin plugin;

    @Getter
    @Setter
    private @NonNull ColorScheme colorScheme;

    @Builder(builderMethodName = "spigotCAPBuilder")
    protected SpigotCAP(final @Nullable DefaultHelpCommandRenderer helpCommandRenderer, final boolean debug,
                        final @NonNull JavaPlugin plugin, final @Nullable ColorScheme colorScheme)
    {
        super(Util.valOrDefault(helpCommandRenderer, SpigotHelpCommandRenderer.getDefault()), debug);
        this.plugin = plugin;
        this.colorScheme = Util.valOrDefault(colorScheme, generateColorScheme());
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

    public @NonNull CommandResult parseInput(final @NotNull CommandSender sender, final @NotNull Command cmd,
                                             final @NotNull String[] args)
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException, EOFException,
               NoPermissionException, ValidationFailureException, IllegalValueException
    {
        final @NonNull ICommandSender commandSender = SpigotCommandSender.wrapCommandSender(sender, colorScheme);

        final String[] fullArgs = new String[args.length + 1];
        fullArgs[0] = cmd.getName();
        System.arraycopy(args, 0, fullArgs, 1, args.length);

        return parseInput(commandSender, fullArgs);
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
            .addStyle(TextType.OPTIONAL_PARAMETER, ChatColor.BLUE, ChatColor.BOLD)
            .addStyle(TextType.OPTIONAL_PARAMETER_FLAG, ChatColor.LIGHT_PURPLE)
            .addStyle(TextType.OPTIONAL_PARAMETER_SEPARATOR, ChatColor.DARK_RED)
            .addStyle(TextType.OPTIONAL_PARAMETER_LABEL, ChatColor.DARK_AQUA)
            .addStyle(TextType.REQUIRED_PARAMETER, ChatColor.RED, ChatColor.BOLD)
            .addStyle(TextType.REQUIRED_PARAMETER_FLAG, ChatColor.DARK_BLUE)
            .addStyle(TextType.REQUIRED_PARAMETER_SEPARATOR, ChatColor.BLACK)
            .addStyle(TextType.REQUIRED_PARAMETER_LABEL, ChatColor.WHITE)
            .addStyle(TextType.SUMMARY, ChatColor.AQUA)
            .addStyle(TextType.REGULAR_TEXT, ChatColor.DARK_PURPLE)
            .addStyle(TextType.HEADER, ChatColor.GREEN)
            .addStyle(TextType.FOOTER, ChatColor.DARK_RED)
            .build();
    }
}
