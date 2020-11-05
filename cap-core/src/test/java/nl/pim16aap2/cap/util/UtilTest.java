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
