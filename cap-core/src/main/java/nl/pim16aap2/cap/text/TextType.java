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

package nl.pim16aap2.cap.text;

import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;

/**
 * Represents the different types of {@link Text} used by CAP. Every type of text can have its own {@link
 * TextComponent}.
 *
 * @author Pim
 */
public enum TextType
{
    /**
     * The name(s) of a command.
     */
    COMMAND,

    /**
     * The name of the value of an optional parameter. E.g. For [-p=player] this would be the '[]' part.
     */
    OPTIONAL_PARAMETER,

    /**
     * The name of the value of an optional parameter. E.g. For [-p=player] this would be the 'player' part.
     */
    OPTIONAL_PARAMETER_LABEL,

    /**
     * The name/flag of an optional parameter. E.g. For [-p=player] this would be the '-p' part.
     */
    OPTIONAL_PARAMETER_FLAG,

    /**
     * The separator of an optional parameter. E.g. For [-p=player] this would be the '=' part.
     */
    OPTIONAL_PARAMETER_SEPARATOR,

    /**
     * A required parameter.
     */
    REQUIRED_PARAMETER,

    /**
     * The name of the value of a required parameter. E.g. For <-p=player> this would be the 'player' part.
     */
    REQUIRED_PARAMETER_LABEL,

    /**
     * The name/flag of a required parameter. E.g. For <-p=player> this would be the '-p' part.
     */
    REQUIRED_PARAMETER_FLAG,

    /**
     * The separator of a required parameter. E.g. For <-p=player> this would be the '=' part.
     */
    REQUIRED_PARAMETER_SEPARATOR,

    /**
     * The {@link Command#getSummary(ICommandSender)} of a {@link Command}.
     */
    SUMMARY,

    /**
     * The {@link Command#getDescription(ICommandSender)} of a {@link Command}.
     */
    DESCRIPTION,

    /**
     * Regular text used for miscellaneous text.
     */
    REGULAR_TEXT,

    /**
     * The {@link Command#getHeader(ICommandSender)} of a {@link Command}.
     */
    HEADER,

    /**
     * The section header. Used for command-specific section headers.
     */
    SECTION,

    /**
     * UNUSED for now.
     */
    FOOTER,

    /**
     * Used for errors and stuff.
     */
    ERROR,
    ;
}
