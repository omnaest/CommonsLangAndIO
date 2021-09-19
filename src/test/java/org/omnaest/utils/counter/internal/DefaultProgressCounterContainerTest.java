package org.omnaest.utils.counter.internal;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.counter.ImmutableProgressCounterContainer.ProgressCounterAccessor;
import org.omnaest.utils.counter.ProgressCounterContainer;

/**
 * @see DefaultProgressCounterContainer
 * @author omnaest
 */
public class DefaultProgressCounterContainerTest
{

    @Test
    public void testAsDurationProgressCounter() throws Exception
    {
        ProgressCounterContainer progressCounterContainer = ProgressCounterContainer.newInstance()
                                                                                    .newDurationProgressCounterWithWeight("counter1", 0.3,
                                                                                                                          counter -> counter.withMaximum(10)
                                                                                                                                            .incrementBy(10))
                                                                                    .newDurationProgressCounterWithWeight("counter2", 0.7,
                                                                                                                          counter -> counter.withMaximum(1)
                                                                                                                                            .increment());

        assertEquals(Arrays.asList("counter1", "counter2"), progressCounterContainer.stream()
                                                                                    .map(ProgressCounterAccessor::getName)
                                                                                    .map(Optional::get)
                                                                                    .collect(Collectors.toList()));
        assertEquals(1.0, progressCounterContainer.getOverallProgress(), 0.001);
        assertEquals(11, progressCounterContainer.getOverallCount());
        assertEquals(11, progressCounterContainer.getOverallMaximum());
    }

}
