package org.omnaest.utils.map.counter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class AtomicIntegerCounterMapTest
{

    @Test
    public void testGet() throws Exception
    {
        IntegerCounterMap<String> counterMap = IntegerCounterMap.newInstance();
        counterMap.incrementByOne("A");
        counterMap.decrementByOne("B")
                  .decrementByOne("B");
        assertEquals(1, counterMap.get("A")
                                  .get()
                                  .intValue());
        assertEquals(-2, counterMap.get("B")
                                   .get()
                                   .intValue());
        assertEquals(1, counterMap.incrementByOneAndGet("C"));
        assertFalse(counterMap.get("NOT_EXISTIMG")
                              .isPresent());
    }

    @Test
    public void testClone() throws Exception
    {
        IntegerCounterMap<String> counterMap = IntegerCounterMap.newInstance();
        counterMap.incrementByOne("A");
        counterMap.decrementByOne("B")
                  .decrementByOne("B");
        IntegerCounterMap<String> clonedCounterMap = counterMap.clone();
        assertEquals(1, clonedCounterMap.get("A")
                                        .get()
                                        .intValue());
        assertEquals(-2, clonedCounterMap.get("B")
                                         .get()
                                         .intValue());
    }

}
