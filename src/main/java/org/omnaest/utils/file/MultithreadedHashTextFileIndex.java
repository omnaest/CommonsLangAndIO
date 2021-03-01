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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.PredicateUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.element.bi.BiElement;

/**
 * Hash based {@link TextFileIndex} which utilizes an internal {@link ExecutorService} to read and write in a concurrent manner.
 * 
 * @author omnaest
 */
public class MultithreadedHashTextFileIndex implements TextFileIndex
{
    private TextFileIndex   fileIndex;
    private ExecutorService executorService;

    public MultithreadedHashTextFileIndex(File directory)
    {
        this(directory, Integer.MAX_VALUE, 2 * Runtime.getRuntime()
                                                      .availableProcessors()
                + 1);
    }

    public MultithreadedHashTextFileIndex(File directory, int capacity, int numberOfThreads)
    {
        super();
        this.fileIndex = new ConcurrentHashTextFileIndex(directory, capacity);
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    }

    @Override
    public TextFileIndex put(String key, String value)
    {
        this.submitToExecutorService(() -> this.fileIndex.put(key, value));
        return this;
    }

    @Override
    public TextFileIndex putAll(Map<String, String> map)
    {
        this.submitToExecutorService(Optional.ofNullable(map)
                                             .orElse(Collections.emptyMap())
                                             .entrySet()
                                             .stream()
                                             .map(entry -> (Callable<?>) () -> this.fileIndex.put(entry.getKey(), entry.getValue()))
                                             .collect(Collectors.toList()));
        return this;
    }

    @Override
    public Map<String, String> getAll(Collection<String> keys)
    {
        List<String> keyList = Optional.ofNullable(keys)
                                       .map(Collection<String>::stream)
                                       .map(Stream<String>::distinct)
                                       .map(stream -> stream.filter(PredicateUtils.notNull())
                                                            .collect(Collectors.toList()))
                                       .orElse(Collections.emptyList());
        return StreamUtils.merge2(keyList.stream(), this.submitToExecutorService(keyList.stream()
                                                                                        .map(entry -> (Callable<Optional<String>>) () -> this.fileIndex.get(entry))
                                                                                        .collect(Collectors.toList()))
                                                        .stream())
                          .filter(entry -> entry.getSecond()
                                                .isPresent())
                          .map(entry -> entry.applyToSecondArgument(Optional::get))
                          .collect(Collectors.toMap(BiElement::getFirst, BiElement::getSecond));
    }

    @Override
    public Optional<String> get(String key)
    {
        return this.submitToExecutorService(() -> this.fileIndex.get(key));
    }

    @Override
    public TextFileIndex clear()
    {
        this.submitToExecutorService(() -> this.fileIndex.clear());
        return this;
    }

    @Override
    public TextFileIndex remove(String key)
    {
        this.submitToExecutorService(() -> this.fileIndex.remove(key));
        return this;
    }

    @Override
    public Stream<String> keys()
    {
        return this.submitToExecutorService(() -> this.fileIndex.keys());
    }

    private <R> List<R> submitToExecutorService(List<Callable<R>> callables)
    {
        return callables.stream()
                        .map(callable -> this.executorService.submit(callable))
                        .collect(Collectors.toList())
                        .stream()
                        .map(future ->
                        {
                            try
                            {
                                return future.get();
                            }
                            catch (InterruptedException | ExecutionException e)
                            {
                                throw new IllegalStateException(e);
                            }
                        })
                        .collect(Collectors.toList());
    }

    private <R> R submitToExecutorService(Callable<R> callable)
    {
        try
        {
            return this.executorService.submit(callable)
                                       .get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws Exception
    {
        this.executorService.shutdown();
    }
}
