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

package nl.pim16aap2.cap.command;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.IntegerArgument;
import nl.pim16aap2.cap.localization.ArgumentNamingSpec;
import nl.pim16aap2.cap.localization.Localizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class ArgumentManagerTest
{
    @Test
    void caseSensitive()
    {
        final @NonNull List<Argument<?>> arguments = Arrays.asList(
            new IntegerArgument().getOptional().identifier("argumenta").nameSpec(
                ArgumentNamingSpec.RawStrings.builder().shortName("argumenta").label("argumenta").build()).build(),

            new IntegerArgument().getOptional().identifier("argumentB").nameSpec(
                ArgumentNamingSpec.RawStrings.builder().shortName("argumentB").label("argumentB").build()).build()
        );

        final @NonNull ArgumentManager argumentManager = new ArgumentManager(new Localizer.Disabled(), arguments, true);
        Assertions.assertEquals("caseSensitive", argumentManager.getArgumentNameCaseCheck("caseSensitive"));

        Assertions.assertTrue(argumentManager.getArgument("argumenta", (Locale) null).isPresent());
        Assertions.assertFalse(argumentManager.getArgument("argumentA", (Locale) null).isPresent());

        Assertions.assertFalse(argumentManager.getArgument("argumentb", (Locale) null).isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentB", (Locale) null).isPresent());
    }

    @Test
    void caseInsensitive()
    {
        final @NonNull List<Argument<?>> arguments = Arrays.asList(
            new IntegerArgument().getOptional().identifier("argumenta").nameSpec(
                ArgumentNamingSpec.RawStrings.builder().shortName("argumenta").label("argumenta").build()).build(),

            new IntegerArgument().getOptional().identifier("argumentB").nameSpec(
                ArgumentNamingSpec.RawStrings.builder().shortName("argumentB").label("argumentB").build()).build()
        );

        final @NonNull ArgumentManager argumentManager = new ArgumentManager(new Localizer.Disabled(),
                                                                             arguments, false);
        Assertions.assertEquals("caseinsensitive", argumentManager.getArgumentNameCaseCheck("caseInsensitive"));

        Assertions.assertTrue(argumentManager.getArgument("argumenta", (Locale) null).isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentA", (Locale) null).isPresent());

        Assertions.assertTrue(argumentManager.getArgument("argumentb", (Locale) null).isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentB", (Locale) null).isPresent());
    }
}
