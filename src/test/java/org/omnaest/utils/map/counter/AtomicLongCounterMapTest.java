package org.omnaest.utils.map.counter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * @see AtomicLongCounterMap
 * @author omnaest
 */
public class AtomicLongCounterMapTest
{

    @Test
    public void testGet() throws Exception
    {
        LongCounterMap<String> counterMap = LongCounterMap.newInstance();
        counterMap.incrementByOne("A");
        counterMap.decrementByOne("B")
                  .decrementByOne("B");
        assertEquals(1l, counterMap.get("A")
                                   .get()
                                   .longValue());
        assertEquals(-2l, counterMap.get("B")
                                    .get()
                                    .longValue());
        assertEquals(1l, counterMap.incrementByOneAndGet("C"));
        assertFalse(counterMap.get("NOT_EXISTIMG")
                              .isPresent());
    }

}
