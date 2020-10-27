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
    /**
     * The {@link ColorScheme} used to add styles to sections of this text.
     */
    @Getter
    private final @NonNull ColorScheme colorScheme;

    /**
     * The {@link StringBuilder} backing this {@link Text} object. All strings appended to this {@link Text} will be
     * stored here.
     */
    private final @NonNull StringBuilder stringBuilder = new StringBuilder();

    /**
     * The list of {@link StyledSection}s.
     */
    private final @NonNull List<StyledSection> styledSections = new ArrayList<>();

    /**
     * The total size of the string held by this object. This is used by {@link #toString()} to instantiate a {@link
     * StringBuilder} of the right size.
     * <p>
     * This value includes both the size of {@link #stringBuilder} as well as the total size of all the {@link
     * #styledSections}.
     */
    private int styledSize = 0;

    @Deprecated
    private static final @NonNull Pattern NEWLINE = Pattern.compile("\n");

    // CopyConstructor
    public Text(final @NonNull Text other)
    {
        colorScheme = other.colorScheme;
        stringBuilder.append(other.stringBuilder);
        other.styledSections.forEach(section -> styledSections.add(new StyledSection(section)));
        styledSize = other.styledSize;
    }

    /**
     * Appends some unstyled text to the current text.
     *
     * @param text The unstyled text to add.
     * @return The current {@link Text} instance.
     */
    public Text add(final @NonNull String text)
    {
        stringBuilder.append(text);
        styledSize += text.length();
        return this;
    }

    /**
     * Appends some styled text to the current text.
     *
     * @param text The text to add.
     * @param type The {@link TextType} of the text to add. The {@link #colorScheme} will be used to look up the style
     *             associated with the type. See {@link ColorScheme#getStyle(TextType)}.
     * @return The current {@link Text} instance.
     */
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

    /**
     * Appends another {@link Text} object to this one.
     * <p>
     * The other {@link Text} object will not be modified.
     *
     * @param other The other {@link Text} instance to append to the current one.
     * @return The current {@link Text} instance.
     */
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

    @Deprecated
    public static @NonNull String[] split(final @NonNull String str)
    {
        return NEWLINE.split(str);
    }

    /**
     * Represents a section in a text that is associated with a certain style.
     *
     * @author Pim
     */
    @AllArgsConstructor
    @Getter
    private static class StyledSection
    {
        private final int startIndex;
        private final int length;
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
