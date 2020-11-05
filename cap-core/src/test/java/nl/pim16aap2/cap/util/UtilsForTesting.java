package nl.pim16aap2.cap.util;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

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
}
