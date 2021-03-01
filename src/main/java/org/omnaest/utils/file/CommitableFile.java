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
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.omnaest.utils.exception.RuntimeIOException;

/**
 * {@link File} wrapper that allows to read and write encapsulated in a transactional or atomar commit
 * 
 * @see #of(File)
 * @see #accept(byte[])
 * @see #get()
 * @author omnaest
 */
public class CommitableFile implements Consumer<byte[]>, Supplier<byte[]>
{
    private static final int DEFAULT_SLOT = 0;

    private File file;

    private CommitableFile(File file)
    {
        super();
        this.file = file;
    }

    public static CommitableFile of(File file)
    {
        return new CommitableFile(file);
    }

    public static interface Transaction
    {
        public Transaction accept(byte[] content, int slot);

        public Transaction accept(String content, int slot);

        public Transaction accept(byte[] content);

        public Transaction accept(String content);

        public CommitableFile commit();
    }

    /**
     * Returns a {@link Transaction} which allows to write content to multiple slots in an atomic commit operation.
     * 
     * @return
     */
    public Transaction transaction()
    {
        byte commitFileState = this.determineCommitFileState();
        byte newCommitFileState = (byte) ((commitFileState + 1) % 2);
        return new Transaction()
        {
            private boolean success = true;

            @Override
            public Transaction accept(byte[] content, int slot)
            {
                try
                {
                    File targetFile = CommitableFile.this.determineTargetFile(newCommitFileState, slot);
                    FileUtils.writeByteArrayToFile(targetFile, content);
                }
                catch (IOException e)
                {
                    this.success = false;
                    throw new RuntimeIOException(e);
                }
                return this;
            }

            @Override
            public Transaction accept(String content, int slot)
            {
                this.accept(Optional.ofNullable(content)
                                    .map(c -> c.getBytes(StandardCharsets.UTF_8))
                                    .orElse(new byte[0]),
                            slot);
                return this;
            }

            @Override
            public CommitableFile commit()
            {
                if (this.success)
                {
                    try
                    {
                        FileUtils.writeByteArrayToFile(CommitableFile.this.determineCommitFile(), new byte[] { newCommitFileState });
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeIOException(e);
                    }
                }
                return CommitableFile.this;
            }

            @Override
            public Transaction accept(byte[] content)
            {
                return this.accept(content, DEFAULT_SLOT);
            }

            @Override
            public Transaction accept(String content)
            {
                return this.accept(content, DEFAULT_SLOT);
            }
        };
    }

    private File determineCommitFile()
    {
        return new File(this.file, "commit.state");
    }

    private File determineCurrentTargetFile(int slot)
    {
        return this.determineTargetFile(this.determineCommitFileState(), slot);
    }

    private File determineTargetFile(byte commitFileState, int slot)
    {
        return new File(this.file, slot + "." + commitFileState);
    }

    private byte determineCommitFileState()
    {
        return Optional.ofNullable(this.determineCommitFile())
                       .filter(File::exists)
                       .map(commitFile ->
                       {
                           try
                           {
                               return FileUtils.readFileToByteArray(commitFile);
                           }
                           catch (IOException e)
                           {
                               throw new RuntimeIOException(e);
                           }
                       })
                       .filter(content -> content.length >= 1)
                       .map(content -> content[0])
                       .orElse((byte) 0);
    }

    /**
     * Returns the file content. If no file content is written yet, then it returns null.
     */
    @Override
    public byte[] get()
    {
        return this.get(DEFAULT_SLOT);
    }

    public byte[] get(int slot)
    {
        return Optional.ofNullable(this.determineCurrentTargetFile(slot))
                       .filter(File::exists)
                       .map(targetFile ->
                       {
                           try
                           {
                               return FileUtils.readFileToByteArray(targetFile);
                           }
                           catch (IOException e)
                           {
                               throw new RuntimeIOException(e);
                           }
                       })
                       .orElse(null);
    }

    /**
     * Writes the file content
     */
    @Override
    public void accept(byte[] content)
    {
        this.accept(content, DEFAULT_SLOT);
    }

    public void accept(byte[] content, int slot)
    {
        this.transaction()
            .accept(content, slot)
            .commit();
    }

    /**
     * Similar to {@link #accept(byte[])} but accepts a {@link String} and writes it as {@link StandardCharsets#UTF_8}
     * 
     * @param content
     */
    public void accept(String content)
    {
        this.accept(content, DEFAULT_SLOT);
    }

    public void accept(String content, int slot)
    {
        this.transaction()
            .accept(content, slot)
            .commit();
    }

    /**
     * Similar to {@link #get()} but returns the content as {@link StandardCharsets#UTF_8} {@link String}.
     * 
     * @return
     */
    public String getAsString()
    {
        return this.getAsString(DEFAULT_SLOT);
    }

    public String getAsString(int slot)
    {
        return Optional.ofNullable(this.get(slot))
                       .map(content -> new String(content, StandardCharsets.UTF_8))
                       .orElse(null);
    }

    public CommitableFile delete()
    {
        FileUtils.deleteQuietly(this.file);
        return this;
    }

}
