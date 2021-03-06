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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a colorscheme that can be used to add styles to text.
 *
 * @author Pim
 */
public class ColorScheme
{
    protected final Map<TextType, TextComponent> styleMap;
    protected final @Nullable String defaultDisable;

    protected ColorScheme(final @NonNull Map<TextType, TextComponent> styleMap, final @Nullable String defaultDisable)
    {
        this.styleMap = styleMap;
        this.defaultDisable = defaultDisable;
    }

    /**
     * Updates the style for a given {@link TextType}.
     *
     * @param type  The {@link TextType} for which to update its style.
     * @param style The new style to use.
     * @return The current {@link ColorScheme} instance.
     */
    @Contract("_, _-> this")
    public ColorScheme setStyle(final @NonNull TextType type, @NonNull TextComponent style)
    {
        if (defaultDisable != null && !style.getOn().equals("") && style.getOff().equals(""))
            style = new TextComponent(style.getOn(), defaultDisable);
        styleMap.put(type, style);
        return this;
    }

    /**
     * Gets the style associated with a certain {@link TextType}.
     *
     * @param type The {@link TextType} for which to find its style.
     * @return The style associated with the given {@link TextType}.
     */
    public @NonNull TextComponent getStyle(final @NonNull TextType type)
    {
        return styleMap.get(type);
    }

    /**
     * Builds a new {@link ColorScheme}.
     *
     * @return A new {@link ColorSchemeBuilder}.
     */
    public static ColorSchemeBuilder builder()
    {
        return new ColorSchemeBuilder();
    }

    public static class ColorSchemeBuilder
    {
        private final Map<TextType, TextComponent> styleMap = new EnumMap<>(TextType.class);

        private @Nullable String defaultDisable = null;

        private static final @NonNull TextComponent EMPTY_STYLE = new TextComponent("", "");


        private ColorSchemeBuilder()
        {
        }

        /**
         * Copies the style of a base type to a number of target types for every target type that does not have a style
         * yet. Target types that do have a style are left as-is.
         * <p>
         * If the base style has not been defined, nothing happens.
         *
         * @param styleMap The {@link #styleMap} to add the defaults to.
         * @param base     The base type whose defined
         * @param targets  The target types to copy the base style to for those that do not have a style yet.
         */
        private static void copyDefaults(final @NonNull Map<TextType, TextComponent> styleMap,
                                         final @NonNull TextType base, final @NonNull TextType... targets)
        {
            final @Nullable TextComponent baseStyle = styleMap.get(base);
            if (baseStyle == null)
                return;
            for (TextType target : targets)
                styleMap.putIfAbsent(target, baseStyle);
        }

        /**
         * Copies the default values for the designated subtypes. See {@link #copyDefaults(Map, TextType,
         * TextType...)}.
         *
         * @param styleMap The {@link #styleMap} to append the default values to if needed.
         */
        private static void copyDefaults(final @NonNull Map<TextType, TextComponent> styleMap)
        {
            copyDefaults(styleMap, TextType.OPTIONAL_PARAMETER, TextType.OPTIONAL_PARAMETER_FLAG,
                         TextType.OPTIONAL_PARAMETER_LABEL, TextType.OPTIONAL_PARAMETER_SEPARATOR);

            copyDefaults(styleMap, TextType.REQUIRED_PARAMETER, TextType.REQUIRED_PARAMETER_FLAG,
                         TextType.REQUIRED_PARAMETER_LABEL, TextType.REQUIRED_PARAMETER_SEPARATOR);
        }

        /**
         * Prepares this object for building.
         * <p>
         * It makes sure all the default values are copied if needed (see {@link #copyDefaults(Map)}, that all missing
         * values get an empty value, and that all styles without disable style are assigned one (if
         * <i>defaultDisable</i> is provided).
         *
         * @param styleMap       The {@link #styleMap} to prepare for building.
         * @param defaultDisable See {@link #defaultDisable}.
         */
        public static void prepareBuild(final @NonNull Map<TextType, TextComponent> styleMap,
                                        final @Nullable String defaultDisable)
        {
            // If defaultDisable was set, apply this default value to any components
            // that do not have an 'off' value yet.
            if (defaultDisable != null)
                for (final Map.Entry<TextType, TextComponent> entry : styleMap.entrySet())
                {
                    final TextComponent component = entry.getValue();
                    // If 'on' is set, but off isn't,
                    if ((!component.getOn().equals("")) &&
                        component.getOff().equals(""))
                        styleMap.put(entry.getKey(), new TextComponent(component.getOn(), defaultDisable));
                }

            copyDefaults(styleMap);

            // If any values are still missing, just add the empty style for those.
            if (styleMap.size() != TextType.class.getEnumConstants().length)
                for (final @NonNull TextType type : TextType.values())
                    styleMap.putIfAbsent(type, EMPTY_STYLE);
        }

        /**
         * Constructs the new {@link ColorScheme}.
         *
         * @return The new {@link ColorScheme}.
         */
        public ColorScheme build()
        {
            prepareBuild(styleMap, defaultDisable);
            return new ColorScheme(styleMap, defaultDisable);
        }

        /**
         * Sets the String that disables all active styles in one go.
         * <p>
         * When set to anything other than null, this will be used as the default value for all {@link TextComponent}s
         * when the on value is not empty and the off value is an empty string.
         *
         * @param str The string that disables all active styles.
         * @return This {@link ColorSchemeBuilder} instance.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder setDefaultDisable(final @Nullable String str)
        {
            defaultDisable = str;
            return this;
        }

        /**
         * Adds a style to a given {@link TextType}.
         *
         * @param type  The {@link TextType} to add a style for.
         * @param style The style to add.
         * @return This {@link ColorSchemeBuilder} instance.
         */
        @Contract("_, _-> this")
        public ColorSchemeBuilder addStyle(final @NonNull TextType type, final @NonNull TextComponent style)
        {
            styleMap.put(type, style);
            return this;
        }

        /**
         * Adds a style to a given {@link TextType}.
         *
         * @param type The {@link TextType} to add a style for.
         * @param on   The String that is used to enable this component. E.g. {@code <it>}.
         * @param off  The String that is used to disable this component. E.g. {@code </it>}.
         * @return This {@link ColorSchemeBuilder} instance.
         */
        @Contract("_, _, _-> this")
        public ColorSchemeBuilder addStyle(final @NonNull TextType type,
                                           final @NonNull String on, final @NonNull String off)
        {
            styleMap.put(type, new TextComponent(on, off));
            return this;
        }

        /**
         * See {@link TextType#COMMAND}.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder commandStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.COMMAND, style);
            return this;
        }

        /**
         * See {@link TextType#OPTIONAL_PARAMETER}.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder optionalParameterStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.OPTIONAL_PARAMETER, style);
            return this;
        }

        /**
         * See {@link TextType#OPTIONAL_PARAMETER_LABEL}.
         * <p>
         * Defaults to the same value used for {@link TextType#OPTIONAL_PARAMETER}
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder optionalParameterLabelStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.OPTIONAL_PARAMETER_LABEL, style);
            return this;
        }

        /**
         * See {@link TextType#OPTIONAL_PARAMETER_FLAG}.
         * <p>
         * Defaults to the same value used for {@link TextType#OPTIONAL_PARAMETER}
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder optionalParameterFlagStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.OPTIONAL_PARAMETER_FLAG, style);
            return this;
        }

        /**
         * See {@link TextType#OPTIONAL_PARAMETER_SEPARATOR}.
         * <p>
         * Defaults to the same value used for {@link TextType#OPTIONAL_PARAMETER}
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder optionalParameterSeparatorStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.OPTIONAL_PARAMETER_SEPARATOR, style);
            return this;
        }

        /**
         * See {@link TextType#REQUIRED_PARAMETER}.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder requiredParameterStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.REQUIRED_PARAMETER, style);
            return this;
        }

        /**
         * See {@link TextType#REQUIRED_PARAMETER_LABEL}.
         * <p>
         * Defaults to the same value used for {@link TextType#REQUIRED_PARAMETER}
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder requiredParameterLabelStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.REQUIRED_PARAMETER_LABEL, style);
            return this;
        }

        /**
         * See {@link TextType#REQUIRED_PARAMETER_FLAG}.
         * <p>
         * Defaults to the same value used for {@link TextType#REQUIRED_PARAMETER}
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder requiredParameterFlagStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.REQUIRED_PARAMETER_FLAG, style);
            return this;
        }

        /**
         * See {@link TextType#REQUIRED_PARAMETER_SEPARATOR}.
         * <p>
         * Defaults to the same value used for {@link TextType#REQUIRED_PARAMETER}
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder requiredParameterSeparatorStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.REQUIRED_PARAMETER_SEPARATOR, style);
            return this;
        }

        /**
         * See {@link TextType#SUMMARY}.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder summaryStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.SUMMARY, style);
            return this;
        }

        /**
         * See {@link TextType#DESCRIPTION}.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder descriptionStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.DESCRIPTION, style);
            return this;
        }

        /**
         * See {@link TextType#REGULAR_TEXT}.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder regularTextStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.REGULAR_TEXT, style);
            return this;
        }

        /**
         * See {@link TextType#HEADER}.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder headerStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.HEADER, style);
            return this;
        }

        /**
         * See {@link TextType#SECTION}.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder sectionStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.SECTION, style);
            return this;
        }

        /**
         * See {@link TextType#FOOTER}.
         */
        @Contract("_ -> this")
        public ColorSchemeBuilder footerStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.FOOTER, style);
            return this;
        }

        // TODO: Update this.
        @Override
        public String toString()
        {
            return "ColorScheme.ColorSchemeBuilder()";
        }
    }
}
