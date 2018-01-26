package org.omnaest.utils.supplier;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * @see OptionalSupplier
 * @author omnaest
 */
public class OptionalSupplierTest
{
    @Test
    public void test()
    {
        assertEquals(Arrays.asList("a", "b", "c"), OptionalSupplier.of(Arrays.asList("a", "b", "c"))
                                                                   .stream()
                                                                   .collect(Collectors.toList()));
    }
}
