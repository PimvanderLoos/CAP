package nl.pim16aap2.cap.commandparser;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents commandline input.
 *
 * @author Pim
 */
class CommandLineInput
{
    /**
     * The pattern for non-escaped quotation marks.
     */
    private static final Pattern NON_ESCAPED_QUOTATION_MARKS = Pattern.compile("(?<!\\\\)\"");


    /**
     * The list of {@link Command}s/{@link Argument}s to parse.
     */
    @Getter
    private final @NonNull List<String> args;

    /**
     * The raw input.
     */
    @Getter
    protected @NonNull String input;

    /**
     * Keeps track of whether all unescaped quotation marks were matched properly.
     * <p>
     * Example of True: '/command --player="that player"'
     * <p>
     * Example of False: '/command --player="that player'
     */
    @Getter
    private boolean completeQuotationMarks = true;

    public CommandLineInput(final @NonNull String input)
    {
        this.input = input;
        args = preprocess(split(input));
    }

    /**
     * Gets the number of arguments stored in {@link #args}.
     *
     * @return The size of {@link #args}.
     */
    public int size()
    {
        return args.size();
    }

    /**
     * Preprocesses the arguments.
     * <p>
     * Any arguments that are split by spaces (and therefore in different entries) while they should be in a single
     * entry (because of quotation marks, e.g. 'name="my name"') will be merged into single entries.
     *
     * @param rawArgs The raw array of arguments split by spaces.
     * @return The list of preprocessed arguments.
     */
    protected @NonNull List<String> preprocess(final @NonNull List<String> rawArgs)
    {
        final ArrayList<@NonNull String> argsList = new ArrayList<>(rawArgs.size());

        // Represents a argument split by a spaces but inside brackets, e.g. '"my door"' should put 'my door' as a
        // single entry.
        @Nullable String arg = null;
        for (int idx = 0; idx < rawArgs.size(); ++idx)
        {
            String entry = rawArgs.get(idx);
            final Matcher matcher = NON_ESCAPED_QUOTATION_MARKS.matcher(entry);
            int count = 0;
            while (matcher.find())
                ++count;

            if (count > 0)
                entry = matcher.replaceAll("");

            // When there's an even number of (non-escaped) quotation marks, it means that there aren't any spaces
            // between them and as such, we can ignore them.
            if (count % 2 == 0)
            {
                // If arg is null, it means that we don't have to append the current block to another one
                // As such, we can add it to the list directly. Otherwise, we can add it to the arg and look for the
                // termination quotation mark in the next string.
                if (arg == null)
                    argsList.add(entry);
                else
                    arg += entry;
            }
            else
            {
                if (arg == null)
                    arg = entry;
                else
                {
                    argsList.add(arg + entry);
                    arg = null;
                }
            }

            if (arg != null && idx == (rawArgs.size() - 1))
                completeQuotationMarks = false;
        }

        // Add all remaining entries.
        if (arg != null)
            argsList.add(arg);

        argsList.trimToSize();
        return argsList;
    }

    /**
     * Splits a string containing a input on spaces while preserving whitespace as trailing whitespace.
     * <p>
     * E.g. <pre>"/mycommand  arg  value"</pre> will return <pre>["/mycommand  ", "arg  ", "value"]</pre>
     *
     * @param input The string to split.
     * @return The input split on spaces.
     */
    public static @NonNull List<String> split(final @NonNull String input)
    {
        final @NonNull List<String> args = new ArrayList<>();
        int startIdx = 0;
        boolean lastWhiteSpace = false;
        for (int idx = 0; idx < input.length(); ++idx)
        {
            final char c = input.charAt(idx);
            if (Character.isWhitespace(c))
                lastWhiteSpace = true;
            else
            {
                if (lastWhiteSpace)
                {
                    args.add(input.substring(startIdx, idx));
                    startIdx = idx;
                }
                lastWhiteSpace = false;
            }
        }
        if (startIdx < input.length())
            args.add(input.substring(startIdx));
        return args;
    }
}
