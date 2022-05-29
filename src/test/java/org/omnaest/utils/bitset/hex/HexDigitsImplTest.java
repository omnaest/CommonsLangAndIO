package org.omnaest.utils.bitset.hex;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

/**
 * @author omnaest
 */
public class HexDigitsImplTest
{

    @Test
    public void testToUpperCaseString() throws Exception
    {
        assertEquals("0", HexDigits.of(0)
                                   .toUpperCaseString());
        assertEquals("F", HexDigits.of(15)
                                   .toUpperCaseString());
        assertEquals("FF", HexDigits.of(255)
                                    .toUpperCaseString());
        assertEquals("100", HexDigits.of(256)
                                     .toUpperCaseString());
        assertEquals(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"), IntStream.rangeClosed(0, 15)
                                                                                                                             .mapToObj(HexDigits::of)
                                                                                                                             .map(HexDigits::toUpperCaseString)
                                                                                                                             .collect(Collectors.toList()));
    }

}
