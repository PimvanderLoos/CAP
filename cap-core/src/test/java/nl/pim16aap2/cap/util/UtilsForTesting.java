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

package nl.pim16aap2.cap.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.localization.ArgumentNamingSpec;
import nl.pim16aap2.cap.localization.CommandNamingSpec;
import nl.pim16aap2.cap.localization.Localizer;
import org.junit.jupiter.api.Assertions;

import java.util.Locale;
import java.util.Optional;

@UtilityClass
public class UtilsForTesting
{

    public static final @NonNull ICommandSender DEFAULT_COMMAND_SENDER = new DefaultCommandSender();
    public static final @NonNull Argument<?> DUMMY_ARGUMENT = Argument.valuesLessBuilder().nameSpec(
        ArgumentNamingSpec.RawStrings.builder().shortName("a").build()).identifier("a").build();

    public static final @NonNull CAP LOCALIZED_CAP =
        CAP.getDefault().toBuilder()
           .localizer(new Localizer("CAPCore", Locale.US))
           .build();

    /**
     * Creates a new {@link CommandNamingSpec.RawStrings} using only a name (the other values aren't set; they are
     * optional).
     *
     * @param name The name of the specification.
     * @return The newly created specification.
     */
    public @NonNull CommandNamingSpec.RawStrings getBasicCommandName(final @NonNull String name)
    {
        return CommandNamingSpec.RawStrings.builder().name(name).build();
    }

    /**
     * Sleeps the thread for a defined amount of time.
     * <p>
     * When interrupted, the test will fail.
     *
     * @param millis The number of milliseconds to sleep for.
     */
    public void sleep(final long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            Assertions.fail("Failed to sleep thread");
        }
    }

    /**
     * Makes sure that an {@link Optional} is both present and that its result matches the provided value.
     *
     * @param optional The {@link Optional} to check.
     * @param val      The value to compare against the value inside the optional.
     * @param <T>      The type of the values to compare.
     */
    public <T> void optionalEquals(final @NonNull Optional<T> optional, final @NonNull T val)
    {
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(val, optional.get());
    }
}
