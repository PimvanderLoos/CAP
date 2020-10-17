package nl.pim16aap2.commandparser.renderer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class TextComponent
{
    @Getter
    private final @NonNull ColorScheme colorScheme;

    private final @NonNull StringBuilder stringBuilder = new StringBuilder();

    private final @NonNull List<StyledSection> styledSections = new ArrayList<>();

    private int styledSize = 0;

    private static final @NonNull Pattern NEWLINE = Pattern.compile("\n");

    // CopyConstructor
    public TextComponent(final @NonNull TextComponent other)
    {
        this.colorScheme = other.colorScheme;
        this.stringBuilder.append(other.stringBuilder);
        other.styledSections.forEach(section -> styledSections.add(new StyledSection(section)));
    }

    public TextComponent add(final @NonNull String text)
    {
        stringBuilder.append(text);
        styledSize += text.length();
        return this;
    }

    public TextComponent add(final @NonNull String text, final @Nullable TextType type)
    {
        if (type != null)
        {
            final @NonNull TextStyle style = colorScheme.getStyle(type);
            styledSections.add(new StyledSection(stringBuilder.length(), text.length(), style));
            styledSize += style.getOn().length() + style.getOff().length();
        }
        return add(text);
    }

    public TextComponent add(final @NonNull TextComponent other)
    {
        if (other.stringBuilder.length() == 0)
            return this;

        final int currentLength = stringBuilder.length();
        other.styledSections.forEach(
            styledSection -> styledSections.add(new StyledSection(styledSection.startIndex + currentLength,
                                                                  styledSection.length,
                                                                  styledSection.style)));

        stringBuilder.append(other.stringBuilder);
        this.styledSize += other.styledSize;
        return this;
    }

    public @NonNull String toString()
    {
        if (stringBuilder.length() == 0)
            return "";

        final @NonNull StringBuilder sb = new StringBuilder(styledSize);
        int lastIdx = 0;
        for (final StyledSection section : styledSections)
        {
            // If there are any parts without any styles.
            if (section.startIndex > lastIdx)
                sb.append(stringBuilder.substring(lastIdx, section.startIndex));
            final int end = section.getEnd();
            sb.append(section.getStyle().getOn())
              .append(stringBuilder.substring(section.getStartIndex(), end))
              .append(section.getStyle().getOff());
            lastIdx = end;
        }

        return sb.toString();
    }

    public static @NonNull String[] split(final @NonNull String str)
    {
        return NEWLINE.split(str);
    }

    @AllArgsConstructor
    @Getter
    private static class StyledSection
    {
        private int startIndex, length;
        private final @NonNull TextStyle style;

        // Copy constructor
        public StyledSection(final @NonNull StyledSection other)
        {
            this.startIndex = other.startIndex;
            this.length = other.length;
            this.style = other.style;
        }

        int getEnd()
        {
            return startIndex + length;
        }
    }
}
