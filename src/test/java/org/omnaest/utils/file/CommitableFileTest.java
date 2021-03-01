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

import java.io.File;
import java.util.stream.IntStream;

import org.junit.Test;
import org.omnaest.utils.FileUtils;

/**
 * @see CommitableFile
 * @author omnaest
 */
public class CommitableFileTest
{
    @Test
    public void testAccept() throws Exception
    {
        File tempDirectory = FileUtils.createRandomTempDirectory();
        CommitableFile file = CommitableFile.of(new File(tempDirectory, "test.dat"));

        IntStream.range(0, 10)
                 .forEach(counter ->
                 {
                     String content = "12345" + counter;
                     file.accept(content);
                     assertEquals(content, file.getAsString());
                     assertEquals(content, file.getAsString());
                 });
    }

    @Test
    public void testGetAsString() throws Exception
    {
        File tempDirectory = FileUtils.createRandomTempDirectory();
        CommitableFile file = CommitableFile.of(new File(tempDirectory, "test2.dat"));

        // read still empty file
        assertEquals(null, file.getAsString());

        // write empty and read empty
        file.accept("");
        assertEquals("", file.getAsString());
    }
}
