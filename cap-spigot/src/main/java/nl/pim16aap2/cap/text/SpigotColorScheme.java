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

package nl.pim16aap2.cap.text;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
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

    /**
     * Updates the style for a given {@link TextType}.
     * <p>
     * The {@link ChatColor}s will be combined into a single style.
     *
     * @param type       The {@link TextType} of the style to update.
     * @param chatColors The {@link ChatColor}s to use as the style for this {@link TextType}.
     * @return The current {@link ColorScheme} instance.
     */
    @Contract("_, _ -> this")
    public SpigotColorScheme setStyle(final @NonNull TextType type, final @NonNull ChatColor... chatColors)
    {
        styleMap.put(type, new TextComponent(SpigotColorSchemeBuilder.flattenChatColors(chatColors),
                                             ChatColor.RESET.toString()));
        return this;
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
        @Contract("_, _ -> this")
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
        @Contract("_, _ -> this")
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
        @Contract("_, _ -> this")
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
        @Contract("_, _ -> this")
        public SpigotColorSchemeBuilder addStyle(final @NonNull TextType type, final @NonNull ChatColor... chatColors)
        {
            styleMap.put(type, new TextComponent(flattenChatColors(chatColors), defaultDisable));
            return this;
        }

        private static @NonNull String flattenChatColors(final @NonNull ChatColor... styles)
        {
            String style = "";
            for (final ChatColor chatColor : styles)
                style += chatColor.toString();
            return style;
        }

        public @NonNull SpigotColorScheme build()
        {
            ColorScheme.ColorSchemeBuilder.prepareBuild(styleMap, defaultDisable);

            return new SpigotColorScheme(styleMap, defaultDisable);
        }
    }
}
