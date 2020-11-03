package nl.pim16aap2.cap;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class CAPTest
{
    @Test
    void split()
    {
        Assertions.assertEquals(5, CAP.split("bigdoors addowner mydoor --player pim16aap2").size());
        Assertions.assertEquals(5, CAP.split("bigdoors addowner mydoor --player    pim16aap2").size());

        final @NonNull List<String> split = CAP.split("bigdoors addowner mydoor --player    pim16aap2   a ");
        Assertions.assertEquals(split.size(), 6);
        Assertions.assertEquals(2, split.get(5).length());
    }
}
