package org.omnaest.utils.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omnaest.utils.PredicateUtils;

public class FirstElementFilterCaptureTest
{
    private FirstElementFilterCapture<String> firstElementFilterCapture = PredicateUtils.firstElementFilterCapture();

    @Test
    public void testTest() throws Exception
    {
        assertFalse(this.firstElementFilterCapture.test("value1"));
        assertEquals("value1", this.firstElementFilterCapture.get());

        assertTrue(this.firstElementFilterCapture.test("value2"));
        assertEquals("value1", this.firstElementFilterCapture.get());

    }

}
