package org.omnaest.utils.file;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.omnaest.utils.lock.LockMap;

/**
 * Concurrent hash based {@link TextFileIndex}
 * 
 * @see TextFileIndex
 * @see HashTextFileIndex
 * @author omnaest
 */
public class ConcurrentHashTextFileIndex implements TextFileIndex
{
    private LockMap       lockMap;
    private TextFileIndex fileIndex;

    public ConcurrentHashTextFileIndex(File directory, int capacity)
    {
        super();
        this.fileIndex = new HashTextFileIndex(directory, capacity);
        this.lockMap = new LockMap();
    }

    @Override
    public TextFileIndex put(String key, String value)
    {
        this.lockMap.run(key, () -> this.fileIndex.put(key, value));
        return this;
    }

    @Override
    public Optional<String> get(String key)
    {
        return this.fileIndex.get(key);
    }

    @Override
    public TextFileIndex clear()
    {
        this.lockMap.run(() -> this.fileIndex.clear());
        return this;
    }

    @Override
    public TextFileIndex remove(String key)
    {
        this.lockMap.run(key, () -> this.fileIndex.remove(key));
        return this;
    }

    @Override
    public Stream<String> keys()
    {
        try
        {
            return this.lockMap.call(() -> this.fileIndex.keys());
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws Exception
    {
        this.fileIndex.close();
    }

    @Override
    public Map<String, String> getAll(Collection<String> keys)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
