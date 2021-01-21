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
