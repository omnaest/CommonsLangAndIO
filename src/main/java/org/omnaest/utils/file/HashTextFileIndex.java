package org.omnaest.utils.file;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.omnaest.utils.PredicateUtils;
import org.omnaest.utils.exception.RuntimeIOException;

/**
 * {@link String} key and value store which relies on a simple hashing and direct file read and write
 * 
 * @author omnaest
 */
public class HashTextFileIndex
{
    private File directory;
    private int  capacity;

    public HashTextFileIndex(File directory)
    {
        super();
        this.directory = directory;
        this.capacity = Integer.MAX_VALUE;
    }

    public HashTextFileIndex(File directory, int capacity)
    {
        super();
        this.directory = directory;
        this.capacity = capacity;
    }

    private File determineTargetFile(int hashCode)
    {
        if (hashCode > 0)
        {
            int singleTokenMaxValue = 256;
            int currentHashCodeToken = hashCode % singleTokenMaxValue;

            File parentFile = this.determineTargetFile(hashCode / singleTokenMaxValue);
            if (parentFile != null)
            {
                return new File(parentFile, "" + currentHashCodeToken);
            }
            else
            {
                return new File(this.directory, "" + currentHashCodeToken);
            }
        }
        else
        {
            return null;
        }
    }

    public HashTextFileIndex put(String key, String value)
    {
        CommitableFile.of(this.determineTargetFile(this.determineHashCode(key)))
                      .transaction()
                      .accept(value, 0)
                      .accept(key, 1)
                      .commit();
        return this;
    }

    public Optional<String> get(String key)
    {
        return Optional.ofNullable(CommitableFile.of(this.determineTargetFile(this.determineHashCode(key)))
                                                 .getAsString());
    }

    private int determineHashCode(String key)
    {
        return key.hashCode() % this.capacity + 1;
    }

    public HashTextFileIndex clear()
    {
        if (this.directory.exists())
        {
            try
            {
                FileUtils.deleteDirectory(this.directory);
            }
            catch (IOException e)
            {
                throw new RuntimeIOException(e);
            }
        }
        return this;
    }

    public HashTextFileIndex remove(String key)
    {
        File targetFile = this.determineTargetFile(this.determineHashCode(key));
        CommitableFile.of(targetFile)
                      .delete();
        return this;
    }

    public Stream<String> keys()
    {
        return FileUtils.listFilesAndDirs(this.directory, FileFilterUtils.directoryFileFilter(), TrueFileFilter.INSTANCE)
                        .stream()
                        .map(file -> CommitableFile.of(file))
                        .map(cf -> cf.getAsString(1))
                        .filter(PredicateUtils.notNull());
    }
}
