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

package nl.pim16aap2.cap.argument.specialized;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.RepeatableArgument;
import nl.pim16aap2.cap.argument.parser.ArgumentParser;
import nl.pim16aap2.cap.command.Command;

/**
 * Represents a specialized type of {@link Argument}.
 *
 * @param <T> The specialized type.
 * @author Pim+
 */
@RequiredArgsConstructor
public abstract class SpecializedArgument<T>
{
    /**
     * The {@link ArgumentParser} that is used to parse the input value (String) into the desired output value.
     */
    protected final @NonNull ArgumentParser<T> parser;

    /**
     * Gets a builder for an optional {@link Argument}.
     *
     * @return A builder for an optional {@link Argument}.
     */
    public Argument.OptionalBuilder<T> getOptional()
    {
        return Argument.<T>optionalBuilder().parser(parser);
    }

    /**
     * Gets a builder for a required {@link Argument}.
     * <p>
     * Note that single required arguments are always positional. Their position is defined by the order in which they
     * are added to a {@link Command}. Repeatable required arguments (see {@link #getRepeatable()} are not positional.
     *
     * @return A builder for a required {@link Argument}.
     */
    public @NonNull Argument.RequiredBuilder<T> getRequired()
    {
        return Argument.<T>requiredBuilder().parser(parser);
    }

    /**
     * Gets a builder for an optional positional {@link Argument}.
     * <p>
     * The position is defined by the order in which they are added to a {@link Command}.
     *
     * @return A builder for an optional {@link Argument}.
     */
    public @NonNull Argument.OptionalPositionalBuilder<T> getOptionalPositional()
    {
        return Argument.<T>optionalPositionalBuilder().parser(parser);
    }

    /**
     * Gets a builder for a repeatable {@link Argument}.
     * <p>
     * When set to required, at least 1 input argument will be required.
     * <p>
     * Unlike a regular required {@link Argument} (see {@link #getRequired()}) a repeatable required argument is not
     * positional.
     *
     * @return A builder for a repeatable {@link Argument}.
     */
    public @NonNull RepeatableArgument.RepeatableArgumentBuilder<T> getRepeatable()
    {
        return RepeatableArgument.<T>repeatableBuilder().parser(parser);
    }
}
