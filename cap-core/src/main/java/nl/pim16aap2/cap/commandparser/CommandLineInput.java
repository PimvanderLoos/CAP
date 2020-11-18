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

package nl.pim16aap2.cap.commandparser;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents commandline input that is prepared so that it can be used for parsing later on.
 *
 * @author Pim
 */
class CommandLineInput
{
    /**
     * The pattern for non-escaped quotation marks.
     */
    private static final @NonNull Pattern NON_ESCAPED_QUOTATION_MARKS = Pattern.compile("(?<!\\\\)\"");

    /**
     * The list of {@link Command}s/{@link Argument}s to parse.
     */
    @Getter
    private final @NonNull List<@NonNull String> args;

    /**
     * The raw input.
     */
    @Getter
    private final @NonNull String rawInput;

    /**
     * Keeps track of whether all unescaped quotation marks were matched properly.
     * <p>
     * Example of True: '/command --player="that player"'
     * <p>
     * Example of False: '/command --player="that player'
     */
    @Getter
    private boolean completeQuotationMarks = true;

    public CommandLineInput(final @NonNull String rawInput)
    {
        this.rawInput = rawInput;
        args = preprocess(split(rawInput));
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
    protected @NonNull List<@NonNull String> preprocess(final @NonNull List<@NonNull String> rawArgs)
    {
        final @NonNull ArrayList<@NonNull String> argsList = new ArrayList<>(rawArgs.size());

        // Represents a argument split by a spaces but inside brackets, e.g. '"my door"' should put 'my door' as a
        // single entry.
        @Nullable String arg = null;
        for (int idx = 0; idx < rawArgs.size(); ++idx)
        {
            String entry = rawArgs.get(idx);
            final @NonNull Matcher matcher = NON_ESCAPED_QUOTATION_MARKS.matcher(entry);
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
    public static @NonNull List<@NonNull String> split(final @NonNull String input)
    {
        final @NonNull List<@NonNull String> args = new ArrayList<>();
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

    @Override
    public @NonNull String toString()
    {
        return "Raw input: \"" + rawInput + "\"\nArguments: " + Util.listToString(getArgs());
    }
}
