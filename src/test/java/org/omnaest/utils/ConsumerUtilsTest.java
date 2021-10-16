package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.omnaest.utils.ConsumerUtils.ListAddingConsumer;

/**
 * @see ConsumerUtils
 * @author omnaest
 */
public class ConsumerUtilsTest
{

    @Test
    public void testNewAddingConsumer() throws Exception
    {
        ListAddingConsumer<String> consumer = ConsumerUtils.newAddingConsumer();
        consumer.accept("a");
        consumer.accept("b");
        assertEquals(Arrays.asList("a", "b"), consumer.get());
    }

}
