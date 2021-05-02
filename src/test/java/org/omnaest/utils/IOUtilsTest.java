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
package org.omnaest.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * @see IOUtils
 * @author omnaest
 */
public class IOUtilsTest
{
    @Test
    public void testCopyWithProgess() throws Exception
    {
        List<Double> progressLog = new ArrayList<>();
        byte[] data = IntStream.range(0, 10000)
                               .mapToObj(i -> "x" + i)
                               .collect(Collectors.joining())
                               .getBytes();
        IOUtils.copyWithProgess(new ByteArrayInputStream(data), new ByteArrayOutputStream(), progressLog::add);
        assertTrue(progressLog.size() >= 2);
        assertTrue(progressLog.stream()
                              .mapToDouble(v -> v)
                              .max()
                              .getAsDouble() <= 1.00001);
        assertTrue(progressLog.stream()
                              .mapToDouble(v -> v)
                              .min()
                              .getAsDouble() >= 0.0);
    }

    @Test
    public void testWriteLines() throws Exception
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.writeLines(Stream.of("a", "b"), outputStream, StandardCharsets.UTF_8, "\n");
        assertEquals("a\nb", new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testCompress() throws Exception
    {
        byte[] data = ("This text should be compressed " + StringUtils.repeat("-", 500)).getBytes();
        byte[] compressedData = IOUtils.compress(data);
        byte[] uncompressedData = IOUtils.uncompress(compressedData);
        assertArrayEquals(data, uncompressedData);
        assertTrue(compressedData.length < uncompressedData.length);
    }

    @Test
    public void testToLineStream() throws Exception
    {
        String content = "abc\ndef\nghi";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        org.apache.commons.io.IOUtils.copy(new StringReader(content), outputStream, StandardCharsets.UTF_8);
        List<String> lines = IOUtils.toLineStream(new ByteArrayInputStream(outputStream.toByteArray()), StandardCharsets.UTF_8)
                                    .collect(Collectors.toList());
        assertNotNull(lines);
        assertEquals(3, lines.size());
        assertEquals(Arrays.asList("abc", "def", "ghi"), lines);
    }
}
