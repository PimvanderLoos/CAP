package nl.pim16aap2.commandparser.renderer;

import lombok.NonNull;

import java.util.EnumMap;
import java.util.Map;

public class ColorScheme
{
    private Map<TextType, TextStyle> styleMap;
    private @NonNull String disableAll = "";

    private ColorScheme(final @NonNull Map<TextType, TextStyle> styleMap)
    {
        this.styleMap = styleMap;
        styleMap.forEach((type, style) -> addStyleOff(style.getOff()));
    }

    private void addStyleOff(final @NonNull String styleOff)
    {
        if (!styleOff.equals("") && !disableAll.contains(styleOff))
            disableAll += styleOff;
    }

    public ColorScheme setStyle(final @NonNull TextType type, final @NonNull TextStyle style)
    {
        styleMap.put(type, style);
        addStyleOff(style.getOff());
        return this;
    }

    public @NonNull TextStyle getStyle(final @NonNull TextType type)
    {
        return styleMap.get(type);
    }

    public static ColorSchemeBuilder builder()
    {
        return new ColorSchemeBuilder();
    }

    public static class ColorSchemeBuilder
    {
        private Map<TextType, TextStyle> styleMap = new EnumMap(TextType.class);

        private static final @NonNull TextStyle EMPTY_STYLE = new TextStyle("", "");

        private ColorSchemeBuilder()
        {
        }

        public ColorScheme build()
        {
            // If any values are still missing, just add the empty style for those.
            if (styleMap.size() != TextType.class.getEnumConstants().length)
                for (final @NonNull TextType type : TextType.values())
                    styleMap.putIfAbsent(type, EMPTY_STYLE);

            return new ColorScheme(styleMap);
        }

        public ColorSchemeBuilder addStyle(final @NonNull TextType type, final @NonNull TextStyle style)
        {
            styleMap.put(type, style);
            return this;
        }

        public ColorSchemeBuilder commandStyle(final @NonNull TextStyle style)
        {
            styleMap.put(TextType.COMMAND, style);
            return this;
        }

        public ColorSchemeBuilder optionalParameterStyle(final @NonNull TextStyle style)
        {
            styleMap.put(TextType.OPTIONAL_PARAMETER, style);
            return this;
        }

        public ColorSchemeBuilder requiredParameterStyle(final @NonNull TextStyle style)
        {
            styleMap.put(TextType.REQUIRED_PARAMETER, style);
            return this;
        }

        public ColorSchemeBuilder summaryStyle(final @NonNull TextStyle style)
        {
            styleMap.put(TextType.SUMMARY, style);
            return this;
        }

        public ColorSchemeBuilder regularTextStyle(final @NonNull TextStyle style)
        {
            styleMap.put(TextType.REGULAR_TEXT, style);
            return this;
        }

        public ColorSchemeBuilder headerStyle(final @NonNull TextStyle style)
        {
            styleMap.put(TextType.HEADER, style);
            return this;
        }

        public ColorSchemeBuilder footerStyle(final @NonNull TextStyle style)
        {
            styleMap.put(TextType.FOOTER, style);
            return this;
        }
        
        public String toString()
        {
            return "ColorScheme.ColorSchemeBuilder()";
        }
    }
}
