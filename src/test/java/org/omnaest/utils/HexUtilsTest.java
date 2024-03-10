package org.omnaest.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HexUtilsTest
{

    @Test
    public void testToHexLong()
    {
        assertEquals("FF", HexUtils.toHex(255L));
    }

}
