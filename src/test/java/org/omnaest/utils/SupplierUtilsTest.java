package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import org.junit.Test;

/**
 * @see SupplierUtils
 * @author omnaest
 */
public class SupplierUtilsTest
{

    @Test
    public void testToSoftReferenceCached() throws Exception
    {
        Iterator<String> iterator = Arrays.asList("a", "b")
                                          .iterator();
        Supplier<String> supplier = SupplierUtils.toSoftReferenceCached(() -> iterator.next());
        assertEquals("a", supplier.get());
        assertEquals("a", supplier.get());
    }

}
