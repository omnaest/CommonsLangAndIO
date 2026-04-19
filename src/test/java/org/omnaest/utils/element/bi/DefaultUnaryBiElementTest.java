package org.omnaest.utils.element.bi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @see DefaultUnaryBiElement
 * @author omnaest
 */
public class DefaultUnaryBiElementTest
{

    @Test
    public void testEqualsWithBiElement()
    {
        assertEquals(BiElement.of(1, 3), UnaryBiElement.of(1, 3));
        assertEquals(UnaryBiElement.of(1, 3), BiElement.of(1, 3));
    }

}
