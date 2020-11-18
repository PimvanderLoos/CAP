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

package nl.pim16aap2.cap;

import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.util.UtilsForTesting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class CAPTest
{
    @Test
    void caseSensitive()
    {
        @NonNull CAP cap =
            CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').caseSensitive(true).build();
        Assertions.assertEquals("caseSensitive", cap.getCommandNameCaseCheck("caseSensitive"));

        Command.commandBuilder().nameSpec(UtilsForTesting.getBasicCommandName("commanda"))
               .virtual(true).cap(cap).build();
        Command.commandBuilder().nameSpec(UtilsForTesting.getBasicCommandName("commandB"))
               .virtual(true).cap(cap).build();

        Assertions.assertTrue(cap.getCommand("commanda", null).isPresent());
        Assertions.assertFalse(cap.getCommand("commandA", null).isPresent());

        Assertions.assertFalse(cap.getCommand("commandb", null).isPresent());
        Assertions.assertTrue(cap.getCommand("commandB", null).isPresent());
    }

    @Test
    void caseInsensitive()
    {
        @NonNull CAP cap =
            CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').caseSensitive(false).build();
        Assertions.assertEquals("caseinsensitive", cap.getCommandNameCaseCheck("caseInsensitive"));

        Command.commandBuilder().nameSpec(UtilsForTesting.getBasicCommandName("commanda"))
               .virtual(true).cap(cap).build();
        Command.commandBuilder().nameSpec(UtilsForTesting.getBasicCommandName("commandB"))
               .virtual(true).cap(cap).build();

        Assertions.assertTrue(cap.getCommand("commanda", null).isPresent());
        Assertions.assertTrue(cap.getCommand("commandA", null).isPresent());

        Assertions.assertTrue(cap.getCommand("commandb", null).isPresent());
        Assertions.assertTrue(cap.getCommand("commandB", null).isPresent());
    }
}
