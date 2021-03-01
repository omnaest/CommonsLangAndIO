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
package org.omnaest.utils.file;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.omnaest.utils.FileUtils;

/**
 * @see HashTextFileIndex
 * @author omnaest
 */
public class HashTextFileIndexTest
{
    private TextFileIndex index = new HashTextFileIndex(FileUtils.createRandomTempDirectoryQuietly()
                                                                     .get());

    @Test
    public void testPutAndGet() throws Exception
    {
        assertEquals(false, this.index.get("1")
                                      .isPresent());
        IntStream.range(0, 10)
                 .forEach(keyCounter -> IntStream.range(0, 10)
                                                 .forEach(counter ->
                                                 {
                                                     String value = "123" + counter;
                                                     String key = "" + keyCounter;
                                                     assertEquals(value, this.index.put(key, value)
                                                                                   .get(key)
                                                                                   .get());
                                                     //                                                     System.out.println(keyCounter + ":" + counter);
                                                 }));
    }

    @Test
    public void testKeys() throws Exception
    {
        IntStream.range(0, 5)
                 .forEach(counter ->
                 {
                     this.index.put("1", "value1" + counter)
                               .put("2", "value2" + counter);
                     assertEquals(Arrays.asList("1", "2")
                                        .stream()
                                        .collect(Collectors.toSet()),
                                  this.index.keys()
                                            .collect(Collectors.toSet()));
                 });
    }

}
