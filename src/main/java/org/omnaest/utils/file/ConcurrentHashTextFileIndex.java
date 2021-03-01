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
