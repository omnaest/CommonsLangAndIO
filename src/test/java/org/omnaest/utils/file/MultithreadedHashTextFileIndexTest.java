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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.omnaest.utils.FileUtils;

public class MultithreadedHashTextFileIndexTest
{
    private TextFileIndex fileIndex = new MultithreadedHashTextFileIndex(FileUtils.createRandomTempDirectoryQuietly()
                                                                                  .get());

    @Test
    public void testPutAndGet() throws Exception
    {
        int numberOfEntries = 100;
        Map<String, String> map = IntStream.range(0, numberOfEntries)
                                           .mapToObj(counter -> "" + counter)
                                           .collect(Collectors.toMap(key -> key, key -> "value" + key));
        this.fileIndex.putAll(map);
        assertEquals(numberOfEntries, this.fileIndex.keys()
                                                    .count());
        map.forEach((key, value) -> assertEquals(value, this.fileIndex.get(key)
                                                                      .get()));
    }

}
