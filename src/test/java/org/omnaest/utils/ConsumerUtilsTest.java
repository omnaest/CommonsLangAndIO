package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.omnaest.utils.ConsumerUtils.ListAddingBiConsumer;
import org.omnaest.utils.ConsumerUtils.ListAddingConsumer;
import org.omnaest.utils.ConsumerUtils.ListAddingTriConsumer;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.element.tri.TriElement;

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

    @Test
    public void testNoOperation() throws Exception
    {
        Arrays.asList()
              .forEach(ConsumerUtils.noOperation());
        MapUtils.builder()
                .build()
                .forEach(ConsumerUtils.noOperation());
    }

    @Test
    public void testConsumeWithAndGetTriElement()
    {
        ListAddingTriConsumer<String, String, String> elementConsumer = ConsumerUtils.newAddingTriConsumer();
        ConsumerUtils.consumeWithAndGet("1", "2", "3", elementConsumer);
        assertEquals(Arrays.asList(TriElement.of("1", "2", "3")), elementConsumer.get());
    }

    @Test
    public void testConsumeWithAndGetBiElement()
    {
        ListAddingBiConsumer<String, String> elementConsumer = ConsumerUtils.newAddingBiConsumer();
        ConsumerUtils.consumeWithAndGet("1", "2", elementConsumer);
        assertEquals(Arrays.asList(BiElement.of("1", "2")), elementConsumer.get());
    }
}
