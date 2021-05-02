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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.omnaest.utils.element.cached.CachedElement;
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

    private final CachedElement<Byte> commitFileState = CachedElement.of(() -> this.determineCommitFileStateFromFile());

    private final File file;

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

        /**
         * Allows direct access to the underlying {@link File}s. Be aware that for partial updates of the file, both underlying files A/B have to be written
         * twice, which can be achieved with calling {@link TransactionWithPartialUpdate#commitFull()}.
         * 
         * @param slot
         * @param fileConsumer
         * @return
         */
        public TransactionWithPartialUpdate operateOnFile(int slot, FileConsumer fileConsumer);

        public TransactionWithPartialUpdate operateOnFiles(FileSlotConsumer fileSlotConsumer);

        public CommitableFile commit();

    }

    public static interface TransactionWithPartialUpdate extends Transaction
    {
        public CommitableFile commitFull();
    }

    public static interface FileConsumer
    {
        void accept(File file) throws IOException;
    }

    public static interface FileSlotConsumer
    {
        void accept(FileProvider fileProvider) throws IOException;
    }

    public static interface FileSlotFunction<R>
    {
        R accept(FileProvider fileProvider) throws IOException;
    }

    public static interface FileProvider
    {
        public File apply(int slot);
    }

    /**
     * Returns a {@link Transaction} which allows to write content to multiple slots in an atomic commit operation.
     * 
     * @return
     */
    public Transaction transaction()
    {
        CachedElement<Byte> newCommitFileState = CachedElement.of(() -> this.calculateNewCommitFileState());
        return new TransactionWithPartialUpdate()
        {
            private boolean success = true;

            private List<Runnable> operations = new ArrayList<>();

            @Override
            public Transaction accept(byte[] content, int slot)
            {
                return this.operateOnFile(slot, targetFile -> FileUtils.writeByteArrayToFile(targetFile, content));
            }

            @Override
            public TransactionWithPartialUpdate operateOnFile(int slot, FileConsumer fileConsumer)
            {
                return this.operateOnFiles(fileProvider -> fileConsumer.accept(fileProvider.apply(slot)));
            }

            @Override
            public TransactionWithPartialUpdate operateOnFiles(FileSlotConsumer fileSlotConsumer)
            {
                this.operations.add(() ->
                {
                    try
                    {
                        fileSlotConsumer.accept(slot -> CommitableFile.this.determineTargetFile(newCommitFileState.get(), slot));
                    }
                    catch (IOException e)
                    {
                        this.success = false;
                        throw new RuntimeIOException(e);
                    }
                });
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
                this.operations.forEach(Runnable::run);
                if (this.success)
                {
                    try
                    {
                        Byte newState = newCommitFileState.getAndReset();
                        FileUtils.writeByteArrayToFile(CommitableFile.this.determineCommitFile(), new byte[] { newState });
                        CommitableFile.this.commitFileState.setSuppliedValue(newState);
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

            @Override
            public CommitableFile commitFull()
            {
                // two commits, so A / B files are written
                this.commit();
                return this.commit();
            }

        };
    }

    private byte calculateNewCommitFileState()
    {
        return this.calculateNewCommitFileState(this.determineCommitFileState());
    }

    private byte calculateNewCommitFileState(byte commitFileState)
    {
        return (byte) ((commitFileState + 1) % 2);
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
        return this.commitFileState.get();
    }

    private byte determineCommitFileStateFromFile()
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

    /**
     * Operates on the current commited files
     * 
     * @param fileSlotConsumer
     * @return
     */
    public CommitableFile operateOnCurrentFiles(FileSlotConsumer fileSlotConsumer)
    {
        try
        {
            fileSlotConsumer.accept(slot -> this.determineCurrentTargetFile(slot));
        }
        catch (IOException e)
        {
            throw new RuntimeIOException(e);
        }
        return this;
    }

    public <R> R operateOnCurrentFiles(FileSlotFunction<R> fileSlotFunction)
    {
        try
        {
            return fileSlotFunction.accept(slot -> this.determineCurrentTargetFile(slot));
        }
        catch (IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    public CommitableFile delete()
    {
        FileUtils.deleteQuietly(this.file);
        return this;
    }

}
