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

package nl.pim16aap2.cap.renderer;

import lombok.Builder;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.localization.Localizer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextType;
import nl.pim16aap2.cap.util.Pair;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Implements the default implementation of the {@link IArgumentRenderer}.
 *
 * @author Pim
 */
public class DefaultArgumentRenderer implements IArgumentRenderer
{
    /**
     * A pair containing the open and close strings for optional brackets.
     * <p>
     * By default this is a pair of {"[", "]"}, resulting in the following format: "[-p=player]"
     */
    protected @NonNull Pair<@NonNull String, @NonNull String> optionalBrackets;

    /**
     * A pair containing the open and close strings for required brackets.
     * <p>
     * By default this is a pair of {"<", ">"}, resulting in the following format: "<-p=player>"
     */
    protected @NonNull Pair<@NonNull String, @NonNull String> requiredBrackets;

    /**
     * @param optionalBrackets See {@link #optionalBrackets}.
     * @param requiredBrackets See {@link #requiredBrackets}.
     */
    @Builder(toBuilder = true)
    protected DefaultArgumentRenderer(final @Nullable Pair<@NonNull String, @NonNull String> optionalBrackets,
                                      final @Nullable Pair<@NonNull String, @NonNull String> requiredBrackets)
    {
        this.optionalBrackets = Util.valOrDefault(optionalBrackets, new Pair<>("[", "]"));
        this.requiredBrackets = Util.valOrDefault(requiredBrackets, new Pair<>("<", ">"));
    }

    /**
     * Gets a new instance of this {@link DefaultArgumentRenderer} using the default values.
     * <p>
     * Use {@link DefaultArgumentRenderer#toBuilder()} if you wish to customize it.
     *
     * @return A new instance of this {@link DefaultArgumentRenderer}.
     */
    public static @NonNull DefaultArgumentRenderer getDefault()
    {
        return DefaultArgumentRenderer.builder().build();
    }

    @Override
    public @NonNull Text render(final @NonNull Localizer localizer, final @Nullable Locale locale,
                                final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument)
    {
        return render(localizer, locale, colorScheme, argument, false);
    }

    @Override
    public @NonNull Text renderLongFormat(final @NonNull Localizer localizer, final @Nullable Locale locale,
                                          final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                          final @NonNull String summaryIndent)
    {
        final Text text = render(localizer, locale, colorScheme, argument);
        if (argument.getLongName(localizer, locale) != null)
            text.add(", ").add(render(localizer, locale, colorScheme, argument, true));

        if (!argument.getSummary(localizer, locale).equals(""))
            text.add("\n").add(summaryIndent)
                .add(localizer.getMessage(argument.getSummary(localizer, locale), locale), TextType.SUMMARY);
        return text;
    }

    /**
     * Renders an argument. See {@link #render(Localizer, Locale, ColorScheme, Argument)}.
     *
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @param useLongName Whether to use {@link Argument#getLongName(Localizer, Locale)}. Please ensure that the {@link
     *                    Argument} has a long name before using this! When this value is false, {@link
     *                    Argument#getShortName(Localizer, Locale)} is used.
     * @return The {@link Text} representing the {@link Argument}.
     */
    protected @NonNull Text render(final @NonNull Localizer localizer, final @Nullable Locale locale,
                                   final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                   final boolean useLongName)
    {
        return argument.isRequired() ?
               renderRequired(localizer, locale, colorScheme, argument, useLongName) :
               renderOptional(localizer, locale, colorScheme, argument, useLongName);
    }

    /**
     * Renders an {@link Argument} in the optional style.
     *
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @param useLongName Whether to use {@link Argument#getLongName(Localizer, Locale)}. Please ensure that the {@link
     *                    Argument} has a long name before using this! When this value is false, {@link
     *                    Argument#getShortName(Localizer, Locale)} is used.
     * @return The {@link Text} representing an optional {@link Argument}.
     */
    protected @NonNull Text renderOptional(final @NonNull Localizer localizer, final @Nullable Locale locale,
                                           final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                           final boolean useLongName)
    {
        final String suffix = argument.isRepeatable() ? "+" : "";
        return new Text(colorScheme).add(optionalBrackets.first, TextType.OPTIONAL_PARAMETER)
                                    .add(renderArgument(localizer, locale, colorScheme, argument, useLongName))
                                    .add(optionalBrackets.second + suffix, TextType.OPTIONAL_PARAMETER);
    }

    /**
     * Renders an {@link Argument} in the required style.
     *
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @param useLongName Whether to use {@link Argument#getLongName(Localizer, Locale)}. Please ensure that the {@link
     *                    Argument} has a long name before using this! When this value is false, {@link
     *                    Argument#getShortName(Localizer, Locale)} is used.
     * @return The {@link Text} representing a required {@link Argument}.
     */
    protected @NonNull Text renderRequired(final @NonNull Localizer localizer, final @Nullable Locale locale,
                                           final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                           final boolean useLongName)
    {
        final String suffix = argument.isRepeatable() ? "+" : "";
        return new Text(colorScheme).add(requiredBrackets.first, TextType.REQUIRED_PARAMETER)
                                    .add(renderArgument(localizer, locale, colorScheme, argument, useLongName))
                                    .add(requiredBrackets.second + suffix, TextType.REQUIRED_PARAMETER);
    }

    /**
     * Renders an argument's name and label (if present). I.e. the part between the optional/required brackets (and as
     * such can be used by both).
     *
     * @param colorScheme The {@link ColorScheme} to use to render the argument.
     * @param argument    The {@link Argument} to render.
     * @param useLongName Whether to use {@link Argument#getLongName(Localizer, Locale)}. Please ensure that the {@link
     *                    Argument} has a long name before using this! When this value is false, {@link
     *                    Argument#getShortName(Localizer, Locale)}  is used.
     * @return The {@link Text} representing the {@link Argument}.
     */
    protected @NonNull Text renderArgument(final @NonNull Localizer localizer, final @Nullable Locale locale,
                                           final @NonNull ColorScheme colorScheme, final @NonNull Argument<?> argument,
                                           final boolean useLongName)
    {
        final @NonNull Text text = new Text(colorScheme);
        @NonNull String argLabel = argument.getLabel(localizer, locale).equals("") ?
                                   argument.getShortName(localizer, locale) :
                                   argument.getLabel(localizer, locale);
        argLabel = localizer.getMessage(argLabel, locale);

        if (argument.isPositional())
            return text.add(argLabel, (argument.isRequired() ?
                                       TextType.REQUIRED_PARAMETER :
                                       TextType.OPTIONAL_PARAMETER_LABEL));

        final TextType name, label, separator;
        if (argument.isRequired())
        {
            name = TextType.REQUIRED_PARAMETER_FLAG;
            label = TextType.REQUIRED_PARAMETER_LABEL;
            separator = TextType.REQUIRED_PARAMETER_SEPARATOR;
        }
        else
        {
            name = TextType.OPTIONAL_PARAMETER_FLAG;
            label = TextType.OPTIONAL_PARAMETER_LABEL;
            separator = TextType.OPTIONAL_PARAMETER_SEPARATOR;
        }

        // TODO: The '-' should be configurable, right?
        @NonNull String argumentName = useLongName ?
                                       ("--" + argument.getLongName(localizer, locale)) :
                                       ("-" + argument.getShortName(localizer, locale));
        argumentName = localizer.getMessage(argumentName, locale);

        text.add(argumentName, name);

        if (argument.isValuesLess())
            return text;

        return text
            .add("=", separator) // FIXME: This should be configurable!
            .add(argLabel, label);
    }
}
