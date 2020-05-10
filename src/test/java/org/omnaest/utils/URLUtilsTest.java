package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class URLUtilsTest
{
    @Test
    public void testRemoveTrailingAnker() throws Exception
    {
        assertEquals("http://www.unit-test.test/", URLUtils.from("http://www.unit-test.test/#lala")
                                                           .removeTrailingAnker()
                                                           .get());
    }
}
