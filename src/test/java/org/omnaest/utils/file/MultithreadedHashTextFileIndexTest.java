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
