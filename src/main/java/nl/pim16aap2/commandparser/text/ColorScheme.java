package nl.pim16aap2.commandparser.text;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class ColorScheme
{
    private final Map<TextType, TextComponent> styleMap;
    private final @Nullable String disableAll;

    protected ColorScheme(final @NonNull Map<TextType, TextComponent> styleMap, final @Nullable String disableAll)
    {
        this.styleMap = styleMap;
        this.disableAll = disableAll;
    }

    public ColorScheme setStyle(final @NonNull TextType type, @NonNull TextComponent style)
    {
        if (disableAll != null && !style.getOn().equals("") && style.getOff().equals(""))
            style = new TextComponent(style.getOn(), disableAll);
        styleMap.put(type, style);
        return this;
    }

    public @NonNull TextComponent getStyle(final @NonNull TextType type)
    {
        return styleMap.get(type);
    }

    public static ColorSchemeBuilder builder()
    {
        return new ColorSchemeBuilder();
    }

    public static class ColorSchemeBuilder
    {
        private final Map<TextType, TextComponent> styleMap = new EnumMap<>(TextType.class);

        private @Nullable String disableAll = null;

        private static final @NonNull TextComponent EMPTY_STYLE = new TextComponent("", "");


        private ColorSchemeBuilder()
        {
        }

        public ColorScheme build()
        {
            // If disableAll was set, apply this default value to any components
            // that do not have an 'off' value yet.
            if (disableAll != null)
                for (final Map.Entry<TextType, TextComponent> entry : styleMap.entrySet())
                {
                    final TextComponent component = entry.getValue();
                    // If 'on' is set, but off isn't,
                    if ((!component.getOn().equals("")) &&
                        component.getOff().equals(""))
                        styleMap.put(entry.getKey(), new TextComponent(component.getOn(), disableAll));
                }

            // If the optionalParameterStyle was set, put the defaults for the 2 'subtypes' if needed.
            final @Nullable TextComponent optionalParameterStyle = styleMap.get(TextType.OPTIONAL_PARAMETER);
            if (optionalParameterStyle != null)
            {
                styleMap.putIfAbsent(TextType.OPTIONAL_PARAMETER_FLAG, optionalParameterStyle);
                styleMap.putIfAbsent(TextType.OPTIONAL_PARAMETER_LABEL, optionalParameterStyle);
                styleMap.putIfAbsent(TextType.OPTIONAL_PARAMETER_SEPARATOR, optionalParameterStyle);
            }

            // If any values are still missing, just add the empty style for those.
            if (styleMap.size() != TextType.class.getEnumConstants().length)
                for (final @NonNull TextType type : TextType.values())
                    styleMap.putIfAbsent(type, EMPTY_STYLE);

            return new ColorScheme(styleMap, disableAll);
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
        public ColorSchemeBuilder setDisableAll(final @Nullable String str)
        {
            this.disableAll = str;
            return this;
        }

        public ColorSchemeBuilder addStyle(final @NonNull TextType type, final @NonNull TextComponent style)
        {
            styleMap.put(type, style);
            return this;
        }

        /**
         * See {@link TextType#COMMAND}.
         */
        public ColorSchemeBuilder commandStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.COMMAND, style);
            return this;
        }

        /**
         * See {@link TextType#OPTIONAL_PARAMETER}.
         */
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
        public ColorSchemeBuilder optionalParameterSeparatorStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.OPTIONAL_PARAMETER_SEPARATOR, style);
            return this;
        }

        /**
         * See {@link TextType#REQUIRED_PARAMETER}.
         */
        public ColorSchemeBuilder requiredParameterStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.REQUIRED_PARAMETER, style);
            return this;
        }

        /**
         * See {@link TextType#SUMMARY}.
         */
        public ColorSchemeBuilder summaryStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.SUMMARY, style);
            return this;
        }

        /**
         * See {@link TextType#DESCRIPTION}.
         */
        public ColorSchemeBuilder descriptionStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.DESCRIPTION, style);
            return this;
        }

        /**
         * See {@link TextType#REGULAR_TEXT}.
         */
        public ColorSchemeBuilder regularTextStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.REGULAR_TEXT, style);
            return this;
        }

        /**
         * See {@link TextType#HEADER}.
         */
        public ColorSchemeBuilder headerStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.HEADER, style);
            return this;
        }

        /**
         * See {@link TextType#FOOTER}.
         */
        public ColorSchemeBuilder footerStyle(final @NonNull TextComponent style)
        {
            styleMap.put(TextType.FOOTER, style);
            return this;
        }

        // TODO: Update this.
        public String toString()
        {
            return "ColorScheme.ColorSchemeBuilder()";
        }
    }
}
