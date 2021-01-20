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
