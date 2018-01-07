package org.omnaest.utils.element.bi;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

/**
 * @see UnaryBiElement
 * @author omnaest
 */
public class UnaryBiElementTest
{
    @Test
    public void testAsList() throws Exception
    {
        assertEquals(Arrays.asList("a", "b"), UnaryBiElement.of("a", "b")
                                                            .asList());
    }

}
