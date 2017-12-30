/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.FileUtils.FileStringContentConsumer;
import org.omnaest.utils.FileUtils.FileStringContentSupplier;

public class FileUtilsTest
{

    @Test
    public void testToSupplier() throws Exception
    {
        File file = FileUtils.createRandomTempFile();
        FileStringContentConsumer consumer = FileUtils.toConsumer(file);
        FileStringContentSupplier supplier = FileUtils.toSupplier(file);

        assertEquals("", supplier.get());

        consumer.accept("text");
        assertEquals("text", supplier.get());
    }

    @Test
    public void testRead() throws Exception
    {
        File tempFile1 = FileUtils.createRandomTempFile();
        File tempFile2 = FileUtils.createRandomTempFile();

        FileUtils.toConsumer(tempFile1)
                 .accept("test1\ntest2");
        FileUtils.toConsumer(tempFile2)
                 .accept("test3\ntest4");

        List<String> lines = FileUtils.read()
                                      .from(tempFile1, tempFile2)
                                      .getAsLinesStream()
                                      .collect(Collectors.toList());
        assertEquals(4, lines.size());
        assertEquals("test1", lines.get(0));
        assertEquals("test2", lines.get(1));
        assertEquals("test3", lines.get(2));
        assertEquals("test4", lines.get(3));
    }

    @Test
    public void readFromAndWriteTo() throws Exception
    {
        File tempFile1 = FileUtils.createRandomTempFile();

        FileUtils.writeTo(tempFile1, writer ->
        {
            try
            {
                writer.write("test");
            } catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        });

        Object object = FileUtils.readFrom(tempFile1, reader ->
        {
            try
            {
                return reader.readLine();
            } catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        });
        assertEquals("test", object);
    }

}
