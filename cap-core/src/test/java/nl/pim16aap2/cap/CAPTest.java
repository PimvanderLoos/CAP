package nl.pim16aap2.cap;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CAPTest
{
    @Test
    void split()
    {
        Assertions.assertEquals(5, CAP.split("bigdoors addowner mydoor --player pim16aap2").length);
        Assertions.assertEquals(5, CAP.split("bigdoors addowner mydoor --player    pim16aap2").length);

        @NonNull String[] split = CAP.split("bigdoors addowner mydoor --player    pim16aap2   a ");
        Assertions.assertEquals(split.length, 6);
        Assertions.assertEquals(2, split[5].length());
    }
}
