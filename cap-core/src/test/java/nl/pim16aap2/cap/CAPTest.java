package nl.pim16aap2.cap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CAPTest
{
    @Test
    void split()
    {
        Assertions.assertEquals(5, CAP.split("bigdoors addowner mydoor --player pim16aap2").length);
        Assertions.assertEquals(5, CAP.split("bigdoors addowner mydoor --player    pim16aap2").length);
    }
}
