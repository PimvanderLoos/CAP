package nl.pim16aap2.cap.text;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class Text
{
    @Getter
    private final @NonNull ColorScheme colorScheme;

    private final @NonNull StringBuilder stringBuilder = new StringBuilder();

    private final @NonNull List<StyledSection> styledSections = new ArrayList<>();

    private int styledSize = 0;

    private static final @NonNull Pattern NEWLINE = Pattern.compile("\n");

    // CopyConstructor
    public Text(final @NonNull Text other)
    {
        colorScheme = other.colorScheme;
        stringBuilder.append(other.stringBuilder);
        other.styledSections.forEach(section -> styledSections.add(new StyledSection(section)));
        styledSize = other.styledSize;
    }

    public Text add(final @NonNull String text)
    {
        stringBuilder.append(text);
        styledSize += text.length();
        return this;
    }

    public Text add(final @NonNull String text, final @Nullable TextType type)
    {
        if (type != null)
        {
            final @NonNull TextComponent style = colorScheme.getStyle(type);
            styledSections.add(new StyledSection(stringBuilder.length(), text.length(), style));
            styledSize += style.getOn().length() + style.getOff().length();
        }
        return add(text);
    }

    public Text add(final @NonNull Text other)
    {
        if (other.stringBuilder.length() == 0)
            return this;

        final int currentLength = stringBuilder.length();

        // Copy everything into an intermediate list to avoid ConcurrentModificationException
        // when adding this text to itself. If it's not the same list, just add it directly.
        final List<StyledSection> target = styledSections == other.styledSections ?
                                           new ArrayList<>(styledSections.size()) : styledSections;
        other.styledSections.forEach(
            styledSection -> target.add(new StyledSection(styledSection.startIndex + currentLength,
                                                          styledSection.length,
                                                          styledSection.style)));

        // If the lists are the same instance, the sections were put in a
        // new map, so append that to the current list.
        if (styledSections == other.styledSections)
            styledSections.addAll(target);

        stringBuilder.append(other.stringBuilder);
        styledSize += other.styledSize;
        return this;
    }

    @Override
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

        // Add any trailing text that doesn't have any styles.
        if (lastIdx < stringBuilder.length())
            sb.append(stringBuilder.substring(lastIdx, stringBuilder.length()));

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
        private final @NonNull TextComponent style;

        // Copy constructor
        public StyledSection(final @NonNull StyledSection other)
        {
            startIndex = other.startIndex;
            length = other.length;
            style = other.style;
        }

        int getEnd()
        {
            return startIndex + length;
        }
    }
}
