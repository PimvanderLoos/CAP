package nl.pim16aap2.commandparser.renderer;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class ColorScheme
{
    private final String resetAllStyles = "";
    private final String commandStyle = "";
    private final String optionalParameterStyle = "";
    private final String requiredParameterStyle = "";
    private final String summaryStyle = "";
    private final String regularTextStyle = "";

    public @NonNull String addStyle(final @NonNull String text, final @NonNull String style)
    {
        return style + text + resetAllStyles;
    }
}
