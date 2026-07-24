package org.omnaest.utils.element.cached;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SingleKeyCachedElementTest
{

    @Test
    public void testApply()
    {
        SingleKeyCachedElement<Integer, String> singleKeyCachedElement = new SingleKeyCachedElement<>();
        assertEquals("A", singleKeyCachedElement.apply(1, () -> "A")
                                                .get());
        assertEquals("A", singleKeyCachedElement.apply(1, null)
                                                .get());
        assertEquals("A", singleKeyCachedElement.apply(1, () -> "B")
                                                .get());
        assertEquals("B", singleKeyCachedElement.apply(2, () -> "B")
                                                .get());
        assertEquals(null, singleKeyCachedElement.apply(1, () -> null)
                                                 .get());
    }

}
