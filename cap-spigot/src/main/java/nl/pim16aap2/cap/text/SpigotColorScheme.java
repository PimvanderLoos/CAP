package nl.pim16aap2.cap.text;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a {@link ColorScheme} class specialized for the Spigot platform.
 *
 * @author Pim
 */
public class SpigotColorScheme extends ColorScheme
{
    protected SpigotColorScheme(final @NonNull Map<TextType, TextComponent> styleMap,
                                final @Nullable String defaultDisable)
    {
        super(styleMap, defaultDisable);
    }

    public static SpigotColorSchemeBuilder spigotColorSchemeBuilder()
    {
        return new SpigotColorSchemeBuilder();
    }

    public static class SpigotColorSchemeBuilder
    {
        private final Map<TextType, TextComponent> styleMap = new EnumMap<>(TextType.class);
        private static final @NonNull TextComponent EMPTY_STYLE = new TextComponent("", "");
        private static final String defaultDisable = ChatColor.RESET.toString();


        SpigotColorSchemeBuilder()
        {
        }

        /**
         * Adds a style to the {@link ColorScheme}.
         *
         * @param type  The {@link TextType} of the style to add.
         * @param style The String representing a style.
         * @return The instance of this builder.
         */
        public SpigotColorSchemeBuilder addStyle(final @NonNull TextType type, final @NonNull TextComponent style)
        {
            styleMap.put(type, style);
            return this;
        }

        /**
         * Adds a style to the {@link ColorScheme}.
         *
         * @param type  The {@link TextType} of the style to add.
         * @param style The String representing a style.
         * @return The instance of this builder.
         */
        public SpigotColorSchemeBuilder addStyle(final @NonNull TextType type, final @NonNull String style)
        {
            styleMap.put(type, new TextComponent(style, defaultDisable));
            return this;
        }

        /**
         * Sets the style of a {@link TextType} to a {@link ChatColor}.
         *
         * @param type      The {@link TextType} of the style to add.
         * @param chatColor The {@link ChatColor} to use as the style for this {@link TextType}.
         * @return The instance of this builder.
         */
        public SpigotColorSchemeBuilder addStyle(final @NonNull TextType type, final @NonNull ChatColor chatColor)
        {
            styleMap.put(type, new TextComponent(chatColor.toString(), defaultDisable));
            return this;
        }

        /**
         * Sets the style of a {@link TextType} to multiple {@link ChatColor}s.
         * <p>
         * The {@link ChatColor}s will be combined into a single style.
         *
         * @param type       The {@link TextType} of the style to add.
         * @param chatColors The {@link ChatColor}s to use as the style for this {@link TextType}.
         * @return The instance of this builder.
         */
        public SpigotColorSchemeBuilder addStyle(final @NonNull TextType type, final @NonNull ChatColor... chatColors)
        {
            styleMap.put(type, new TextComponent(flattenChatColors(chatColors), defaultDisable));
            return this;
        }

        private @NonNull String flattenChatColors(final @NonNull ChatColor... styles)
        {
            String style = "";
            for (final ChatColor chatColor : styles)
                style += chatColor.toString();
            return style;
        }

        public SpigotColorScheme build()
        {
            ColorScheme.ColorSchemeBuilder.prepareBuild(styleMap, defaultDisable);

            return new SpigotColorScheme(styleMap, defaultDisable);
        }
    }
}
