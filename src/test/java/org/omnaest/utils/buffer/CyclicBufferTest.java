/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.omnaest.utils.buffer;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * @see CyclicBuffer
 * @author omnaest
 */
public class CyclicBufferTest
{

    @Test
    public void testAsIterator() throws Exception
    {
        List<String> sourceList = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        CyclicBuffer<String> buffer = new CyclicBuffer<String>(5).withSource(sourceList);

        String result = buffer.asStream()
                              .map(window -> window.get())
                              .collect(Collectors.joining());

        assertEquals(sourceList.stream()
                               .collect(Collectors.joining()),
                     result);

    }

    @Test
    public void testReadFromBufferLeft() throws Exception
    {
        List<String> sourceList = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        CyclicBuffer<String> buffer = new CyclicBuffer<String>(5).withSource(sourceList);

        String result = buffer.asStream()
                              .flatMap(window -> window.getBefore(1)
                                                       .stream())
                              .collect(Collectors.joining());

        assertEquals(sourceList.stream()
                               .collect(Collectors.joining()),
                     result + "h");
    }

    @Test
    public void testReadFromBufferRight() throws Exception
    {
        List<String> sourceList = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        CyclicBuffer<String> buffer = new CyclicBuffer<String>(5).withSource(sourceList);

        String result = buffer.asStream()
                              .flatMap(window -> window.getAfter(1)
                                                       .stream())
                              .collect(Collectors.joining());

        assertEquals(sourceList.stream()
                               .collect(Collectors.joining()),
                     "a" + result);
    }

    @Test
    public void testReadFromBufferBlock() throws Exception
    {
        List<String> sourceList = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        CyclicBuffer<String> buffer = new CyclicBuffer<String>(3).withSource(sourceList);

        String result = buffer.asStream()
                              .flatMap(window -> window.getWindow(1, 1)
                                                       .stream())
                              .collect(Collectors.joining());

        assertEquals(Arrays.asList("ab", "abc", "bcd", "cde", "def", "efg", "fgh", "gh")
                           .stream()
                           .collect(Collectors.joining()),
                     result);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFromBufferBlockWithIndexOutOfBoundsException()
    {
        List<String> sourceList = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        CyclicBuffer<String> buffer = new CyclicBuffer<String>(2).withSource(sourceList);

        String result = buffer.asStream()
                              .flatMap(window -> window.getWindow(1, 1)
                                                       .stream())
                              .collect(Collectors.joining());

        assertEquals(Arrays.asList("ab", "abc", "bcd", "cde", "def", "efg", "fgh", "gh")
                           .stream()
                           .collect(Collectors.joining()),
                     result);
    }

}
