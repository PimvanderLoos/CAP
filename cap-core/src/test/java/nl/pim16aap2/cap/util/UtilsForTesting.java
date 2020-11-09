package nl.pim16aap2.cap.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

@UtilityClass
public class UtilsForTesting
{
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
        System.out.println(optional.get());
        Assertions.assertEquals(val, optional.get());
    }
}
