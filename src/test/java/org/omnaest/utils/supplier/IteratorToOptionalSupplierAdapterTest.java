package org.omnaest.utils.supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Test;

public class IteratorToOptionalSupplierAdapterTest
{

    @Test
    public void testGet() throws Exception
    {
        Iterator<String> iterator = Arrays.asList("a", "b", "c")
                                          .iterator();
        Supplier<Optional<String>> supplier = OptionalSupplier.of(iterator);

        {
            Optional<String> optional = supplier.get();
            assertTrue(optional.isPresent());
            assertEquals("a", optional.get());
        }
        {
            Optional<String> optional = supplier.get();
            assertTrue(optional.isPresent());
            assertEquals("b", optional.get());
        }
        {
            Optional<String> optional = supplier.get();
            assertTrue(optional.isPresent());
            assertEquals("c", optional.get());
        }
        {
            Optional<String> optional = supplier.get();
            assertFalse(optional.isPresent());
        }
    }

}
