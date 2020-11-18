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
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;

class UtilTest
{
    private <T> void assertOptionalEquals(final @NonNull Optional<T> optional, final @Nullable T val)
    {
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(val, optional.get());
    }

    @Test
    void valOrDefault()
    {
        String a = null;
        String b = "test";
        Assertions.assertEquals("test", Util.valOrDefault(a, b));

        a = "a string";
        Assertions.assertEquals("a string", Util.valOrDefault(a, b));
    }

    @Test
    void valOrDefaultSupplier()
    {
        String a = null;
        Assertions.assertEquals("test", Util.valOrDefault(a, () -> "test"));

        a = "a string";
        Assertions.assertEquals("a string", Util.valOrDefault(a, () -> "test"));
    }

    @Test
    void searchIterable()
    {
        final @NonNull List<String> iterable = new ArrayList<>(Arrays.asList("a", "b", "c"));

        assertOptionalEquals(Util.searchIterable(iterable, (val) -> val.equals("b")), "b");

        Assertions.assertFalse(Util.searchIterable(iterable, (val) -> val.equals("d")).isPresent());
    }

    @Test
    void parseInt()
    {
        Assertions.assertFalse(Util.parseInt(null).isPresent());
        @NonNull OptionalInt parsed = Util.parseInt("1");
        Assertions.assertTrue(parsed.isPresent());
        Assertions.assertEquals(1, parsed.getAsInt());
        Assertions.assertFalse(Util.parseInt("number").isPresent());
    }

    @Test
    void parseDouble()
    {
        Assertions.assertFalse(Util.parseDouble(null).isPresent());
        @NonNull OptionalDouble parsed = Util.parseDouble("1.5");
        Assertions.assertTrue(parsed.isPresent());
        Assertions.assertEquals(1.5, parsed.getAsDouble());
        Assertions.assertFalse(Util.parseInt("number").isPresent());
    }

    @Test
    void parseLong()
    {
        Assertions.assertFalse(Util.parseLong(null).isPresent());
        @NonNull OptionalLong parsed = Util.parseLong("1");
        Assertions.assertTrue(parsed.isPresent());
        Assertions.assertEquals(1, parsed.getAsLong());
        Assertions.assertFalse(Util.parseInt("number").isPresent());
    }

    @Test
    void parseUUID()
    {
        final @NonNull UUID uuid = UUID.randomUUID();
        assertOptionalEquals(Util.parseUUID(uuid.toString()), uuid);

        Assertions.assertFalse(Util.parseUUID("NOT A UUID").isPresent());
        Assertions.assertFalse(Util.parseUUID(null).isPresent());
    }

    @Test
    void between()
    {
        Assertions.assertFalse(Util.between(10, 10, 20));
        Assertions.assertFalse(Util.between(20, 10, 20));
        Assertions.assertTrue(Util.between(15, 10, 20));
    }
}
