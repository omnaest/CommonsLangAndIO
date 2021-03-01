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
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.omnaest.utils.EncoderUtils;
import org.omnaest.utils.PredicateUtils;
import org.omnaest.utils.exception.RuntimeIOException;

/**
 * {@link String} key and value store which relies on a simple hashing and direct file read and write
 * 
 * @see TextFileIndex
 * @see ConcurrentHashTextFileIndex
 * @author omnaest
 */
public class HashTextFileIndex implements TextFileIndex
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

    private File determineTargetFile(String key)
    {
        String suffix = "_" + org.omnaest.utils.StringUtils.limitText(EncoderUtils.newInstance()
                                                                                  .forAlphaNumericText()
                                                                                  .encode(key),
                                                                      100, "");
        return this.determineTargetFile(this.determineHashCode(key), key, suffix);
    }

    private File determineTargetFile(int hashCode, String key, String suffix)
    {
        if (hashCode > 0)
        {
            int singleTokenMaxValue = 256;
            int currentHashCodeToken = hashCode % singleTokenMaxValue;

            File parentFile = this.determineTargetFile(hashCode / singleTokenMaxValue, key, "");
            if (parentFile != null)
            {
                return new File(parentFile, currentHashCodeToken + suffix);
            }
            else
            {
                return new File(this.directory, currentHashCodeToken + suffix);
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public TextFileIndex put(String key, String value)
    {
        CommitableFile.of(this.determineTargetFile(key))
                      .transaction()
                      .accept(value, 0)
                      .accept(key, 1)
                      .commit();
        return this;
    }

    @Override
    public Optional<String> get(String key)
    {
        return Optional.ofNullable(CommitableFile.of(this.determineTargetFile(key))
                                                 .getAsString());
    }

    private int determineHashCode(String key)
    {
        return Math.abs(key.hashCode()) % this.capacity + 1;
    }

    @Override
    public TextFileIndex clear()
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

    @Override
    public TextFileIndex remove(String key)
    {
        File targetFile = this.determineTargetFile(key);
        CommitableFile.of(targetFile)
                      .delete();
        return this;
    }

    @Override
    public Stream<String> keys()
    {
        return FileUtils.listFilesAndDirs(this.directory, FileFilterUtils.directoryFileFilter(), TrueFileFilter.INSTANCE)
                        .stream()
                        .map(file -> CommitableFile.of(file))
                        .map(cf -> cf.getAsString(1))
                        .filter(PredicateUtils.notNull());
    }

    @Override
    public void close() throws Exception
    {
        // do nothing        
    }

    @Override
    public Map<String, String> getAll(Collection<String> keys)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
