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

package nl.pim16aap2.cap.util.cache;

import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.cap.util.UtilsForTesting;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.ref.SoftReference;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;

class TimedCacheTest
{
    private final MockClock clock = new MockClock(0);

    /**
     * Make sure that expired values cannot be retreived.
     */
    @Test
    void testExpiry()
    {
        final @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                                null, false, false);
        timedCache.put("key", "value");
        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());

        clock.setCurrentMillis(80);
        timedCache.put("key2", "value2");
        Assertions.assertTrue(timedCache.get("key2").isPresent());
        Assertions.assertEquals(2, timedCache.getSize());

        clock.setCurrentMillis(150);
        Assertions.assertEquals(2, timedCache.getSize());
        Assertions.assertFalse(timedCache.get("key").isPresent());
        Assertions.assertTrue(timedCache.get("key2").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());

        clock.setCurrentMillis(200);
        timedCache.cleanupCache();
        Assertions.assertEquals(0, timedCache.getSize());
    }

    /**
     * Make sure that configuring the cache to use {@link SoftReference}s actually wraps the values in them and that
     * they behave properly when cleared.
     */
    @Test
    void testSoftReference()
    {
        @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                          null, true, false);
        timedCache.put("key", "value");

        @Nullable AbstractTimedValue<String> retrieved = timedCache.getRaw("key");
        Assertions.assertNotNull(retrieved);
        Assertions.assertTrue(retrieved instanceof TimedSoftValue);

        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());
        ((TimedSoftValue<String>) retrieved).getRawValue().clear();
        Assertions.assertFalse(timedCache.get("key").isPresent());
        Assertions.assertEquals(0, timedCache.getSize());


        // Now test that setting softreference to false doesn't wrap values in them.
        timedCache = new TimedCache<>(clock, Duration.ofMillis(100), null, false, false);
        timedCache.put("key", "value");
        retrieved = timedCache.getRaw("key");
        Assertions.assertNotNull(retrieved);
        Assertions.assertFalse(retrieved instanceof TimedSoftValue);
    }

    /**
     * Make sure that refreshing values works properly.
     * <p>
     * Refreshing values should make sure that they don't expire after the expiry time after their last access, not
     * after their insertion.
     */
    @Test
    void testRefresh()
    {
        @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                          null, false, true);
        timedCache.put("key", "value");
        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());

        clock.setCurrentMillis(90);
        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());

        clock.setCurrentMillis(110);
        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());

        clock.setCurrentMillis(220);
        Assertions.assertFalse(timedCache.get("key").isPresent());
        Assertions.assertEquals(0, timedCache.getSize());
    }

    /**
     * Make sure that {@link TimedCache#computeIfAbsent(Object, Function)} works properly.
     * <p>
     * It should insert new values if they don't exist yet, update existing ones if they have expired and not do
     * anything in any other case.
     */
    @Test
    void computeIfAbsent()
    {
        @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                          null, false, false);
        timedCache.computeIfAbsent("key", (k) -> "value");
        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());

        @NonNull String returned = timedCache.computeIfAbsent("key", (k) -> "newVal");
        Assertions.assertEquals("value", returned);

        clock.setCurrentMillis(110);
        returned = timedCache.computeIfAbsent("key", (k) -> "newVal");
        Assertions.assertEquals("newVal", returned);
    }

    @Test
    void computeIfPresent()
    {
        @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                          null, false, false);
        timedCache.put("key", "value");

        UtilsForTesting.optionalEquals(timedCache.computeIfPresent("key", (k, v) -> "newValue"), "newValue");

        Assertions.assertFalse(timedCache.computeIfPresent("key2", (k, v) -> "newValue").isPresent());

        clock.setCurrentMillis(110);

        Assertions.assertFalse(timedCache.computeIfPresent("key", (k, v) -> "newValue").isPresent());
    }

    @Test
    void compute()
    {
        @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                          null, false, false);
        @NonNull String returned = timedCache.compute("key", (k, v) -> v == null ? "value" : (v + v));
        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());
        Assertions.assertEquals("value", returned);

        returned = timedCache.compute("key", (k, v) -> v == null ? "value" : (v + v));
        Assertions.assertEquals("valuevalue", returned);

        clock.setCurrentMillis(110);
        returned = timedCache.compute("key", (k, v) -> v == null ? "newVal" : (v + v));
        Assertions.assertEquals("newVal", returned);
    }

    @Test
    void putIfAbsent()
    {
        @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                          null, false, false);

        UtilsForTesting.optionalEquals(timedCache.putIfAbsent("key", "value"), "value");

        Assertions.assertFalse(timedCache.putIfAbsent("key", "value").isPresent());

        clock.setCurrentMillis(110);

        UtilsForTesting.optionalEquals(timedCache.putIfAbsent("key", "newValue"), "newValue");
    }

    @Test
    void putIfPresent()
    {
        @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                          null, false, false);

        timedCache.put("key", "value");

        UtilsForTesting.optionalEquals(timedCache.putIfPresent("key", "newValue"), "newValue");

        Assertions.assertFalse(timedCache.putIfPresent("key2", "value").isPresent());

        clock.setCurrentMillis(110);

        Assertions.assertFalse(timedCache.putIfPresent("key", "value").isPresent());
    }

    /**
     * Ensure that removing keys from the cache works as intended.
     */
    @Test
    void remove()
    {
        final @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                                null, false, false);
        timedCache.put("key", "value");
        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());

        final @NonNull Optional<String> result = timedCache.remove("key");
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("value", result.get());
        Assertions.assertEquals(0, timedCache.getSize());

        timedCache.put("key", "value");
        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());
        timedCache.clear();
        Assertions.assertEquals(0, timedCache.getSize());
    }

    /**
     * Make sure that the cleanup task properly cleans up any items it should.
     */
    @Test
    void cleanupTask()
    {
        final @NonNull TimedCache<String, String> timedCache = new TimedCache<>(clock, Duration.ofMillis(100),
                                                                                Duration.ofMillis(1), false, false);

        timedCache.put("key", "value");
        Assertions.assertTrue(timedCache.get("key").isPresent());
        Assertions.assertEquals(1, timedCache.getSize());
        UtilsForTesting.sleep(3);
        Assertions.assertEquals(1, timedCache.getSize());

        clock.setCurrentMillis(200);
        UtilsForTesting.sleep(3);
        Assertions.assertEquals(0, timedCache.getSize());
    }

    /**
     * Clock that displays a determined millisecond value which can be set/updated manually.
     *
     * @author Pim
     */
    private static final class MockClock extends Clock
    {
        @Setter
        private long currentMillis;

        public MockClock(final long currentMillis)
        {
            this.currentMillis = currentMillis;
        }

        @Override
        public long millis()
        {
            return currentMillis;
        }

        public void realTime()
        {
            currentMillis = System.currentTimeMillis();
        }

        @Override
        public ZoneId getZone()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Clock withZone(ZoneId zone)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant()
        {
            throw new UnsupportedOperationException();
        }
    }
}
