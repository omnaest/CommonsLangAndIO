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
package org.omnaest.utils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.exception.handler.ExceptionHandler;

/**
 * Helper for {@link ExecutorService} instances and to achieve parallel execution.
 * 
 * @author omnaest
 */
public class ExecutorUtils
{

    public static interface ParallelExecution
    {
        public ParallelExecution withNumberOfThreads(int numberOfThreads);

        public ParallelExecution withNumberOfThreadsPerCPUCore(double numberOfThreadsPerCPUCore);

        public ParallelExecution withNumberOfThreadsLikeAvailableCPUCores();

        public ParallelExecution withUnlimitedNumberOfThreads();

        public ParallelExecution withSingleThread();

        public ParallelExecution withTimeout(long duration, TimeUnit timeUnit);

        public ParallelExecution execute(Consumer<ParallelExecutionCollector> collectorConsumer);

        public <R> ParallelExecutionAndResult<R> executeTasks(Collection<Callable<R>> tasks);

        public <R> ParallelExecutionAndResult<R> executeTasks(Stream<Callable<R>> tasks);

        public <R> ParallelExecutionAndResult<R> executeOperations(Stream<Runnable> operations);

        public <R> ParallelExecutionAndResult<R> executeOperation(Runnable operation);

        public ParallelExecution withExceptionHandler(ExceptionHandler exceptionHandler);

        public AsynchronousParallelExecution asynchronously();

    }

    public static interface AsynchronousParallelExecution extends ParallelExecution
    {
    }

    public static interface ParallelExecutionCollector
    {

        public <R> Supplier<R> addTask(Callable<R> callable);

        public <R> Supplier<R> add(Supplier<R> supplier);

    }

    public static interface ParallelExecutionAndResult<R> extends Supplier<Stream<R>>
    {
        public ParallelExecution and();

        @Override
        public Stream<R> get();

        public ParallelExecutionAndResult<R> consume(Consumer<Stream<R>> resultConsumer);

        /**
         * Resolves all {@link Future}s and pass possible {@link Exception}s to the {@link ExceptionHandler} defined before.
         * 
         * @see ParallelExecution#with
         * @return
         */
        public ParallelExecutionAndResult<R> handleExceptions();
    }

    public static class InterruptedRuntimeException extends RuntimeException
    {
        private static final long serialVersionUID = 8842612133296432488L;

        public InterruptedRuntimeException(Throwable cause)
        {
            super(cause);
        }

    }

    public static ParallelExecution parallel()
    {
        return new ParallelExecutionImpl();
    }

    /**
     * Returns an {@link ExecutorService} with a fixed number of threads per available CPU core
     * 
     * @param numberOfThreadsPerCPUCore
     * @return
     */
    public static ExecutorService newFixedThreadPoolWithNumberOfThreadsPerCPUCore(double numberOfThreadsPerCPUCore)
    {
        int availableProcessors = Runtime.getRuntime()
                                         .availableProcessors();
        int numberOfThreads = (int) (1 + Math.round(availableProcessors * numberOfThreadsPerCPUCore));
        return Executors.newFixedThreadPool(numberOfThreads);
    }

    public static int calculateNumberOfThreadsByPerCPU(double numberOfThreadsPerCPUCore)
    {
        return (int) Math.max(1, Math.round(Runtime.getRuntime()
                                                   .availableProcessors()
                * numberOfThreadsPerCPUCore));
    }

    private static class ParallelExecutionImpl implements ParallelExecution, AsynchronousParallelExecution
    {
        private Supplier<ExecutorService> executorServiceFactory = () -> Executors.newCachedThreadPool();
        private long                      timeout                = Integer.MAX_VALUE;
        private TimeUnit                  timeoutTimeUnit        = TimeUnit.SECONDS;
        private ExceptionHandler          exceptionHandler       = ExceptionHandler.noOperationExceptionHandler();
        private boolean                   asynchronousExecution  = false;
    
        @Override
        public ParallelExecution withNumberOfThreads(int numberOfThreads)
        {
            this.executorServiceFactory = () -> Executors.newFixedThreadPool(numberOfThreads);
            return this;
        }
    
        @Override
        public ParallelExecution withUnlimitedNumberOfThreads()
        {
            return this.withNumberOfThreads(Integer.MAX_VALUE);
        }
    
        @Override
        public ParallelExecution withSingleThread()
        {
            return this.withNumberOfThreads(1);
        }
    
        @Override
        public ParallelExecution withNumberOfThreadsPerCPUCore(double numberOfThreadsPerCPUCore)
        {
            int numberOfThreads = calculateNumberOfThreadsByPerCPU(numberOfThreadsPerCPUCore);
            return this.withNumberOfThreads(numberOfThreads);
        }
    
        @Override
        public ParallelExecution withNumberOfThreadsLikeAvailableCPUCores()
        {
            return this.withNumberOfThreadsPerCPUCore(1.0);
        }
    
        @Override
        public ParallelExecution withTimeout(long duration, TimeUnit timeUnit)
        {
            this.timeout = duration;
            this.timeoutTimeUnit = timeUnit;
            return this;
        }
    
        @Override
        public ParallelExecution withExceptionHandler(ExceptionHandler exceptionHandler)
        {
            this.exceptionHandler = exceptionHandler;
            return this;
        }
    
        @Override
        public ParallelExecution execute(Consumer<ParallelExecutionCollector> collectorConsumer)
        {
            this.executeWithService(executorService ->
            {
                ParallelExecutionCollector collector = new ParallelExecutionCollector()
                {
                    @Override
                    public <R> Supplier<R> addTask(Callable<R> callable)
                    {
                        Future<R> future = executorService.submit(callable);
                        return () ->
                        {
                            try
                            {
                                return future.get(ParallelExecutionImpl.this.timeout, ParallelExecutionImpl.this.timeoutTimeUnit);
                            }
                            catch (InterruptedException | ExecutionException | TimeoutException e)
                            {
                                ParallelExecutionImpl.this.exceptionHandler.accept(e);
                                return null;
                            }
                        };
                    }
    
                    @Override
                    public <R> Supplier<R> add(Supplier<R> supplier)
                    {
                        return this.addTask((Callable<R>) () -> supplier.get());
                    }
    
                };
    
                collectorConsumer.accept(collector);
            });
    
            return this;
        }
    
        public ParallelExecution executeWithService(Consumer<ExecutorService> executorServiceConsumer)
        {
            ExecutorService executorService = this.executorServiceFactory.get();
            try
            {
                if (executorServiceConsumer != null)
                {
                    executorServiceConsumer.accept(executorService);
                }
            }
            finally
            {
                //
                executorService.shutdown();
    
                //
                this.runAwaitShutdownThread(executorService);
            }
            return this;
        }
    
        private void runAwaitShutdownThread(ExecutorService executorService)
        {
            Runnable mainExecutorServiceShutdownOperation = this.createExecutorServiceShutdownOperation(executorService);
            if (this.asynchronousExecution)
            {
                ExecutorService shutdownExecutor = Executors.newSingleThreadExecutor();
                shutdownExecutor.execute(mainExecutorServiceShutdownOperation);
                shutdownExecutor.shutdown();
            }
            else
            {
                mainExecutorServiceShutdownOperation.run();
            }
        }
    
        private Runnable createExecutorServiceShutdownOperation(ExecutorService executorService)
        {
            return () ->
            {
                try
                {
                    executorService.awaitTermination(this.timeout, this.timeoutTimeUnit);
                }
                catch (InterruptedException e)
                {
                    throw new InterruptedRuntimeException(e);
                }
                executorService.shutdownNow();
            };
        }
    
        @Override
        public <R> ParallelExecutionAndResult<R> executeTasks(Collection<Callable<R>> tasks)
        {
            return this.executeTasks(tasks.stream());
        }
    
        @Override
        public <R> ParallelExecutionAndResult<R> executeTasks(Stream<Callable<R>> tasks)
        {
            AtomicReference<List<Future<R>>> result = new AtomicReference<>();
            this.executeWithService(executorService ->
            {
                result.set(tasks.map(task -> executorService.submit(task))
                                .collect(Collectors.toList()));
            });
    
            ParallelExecution parallelExecution = this;
    
            return new ParallelExecutionAndResult<R>()
            {
    
                @Override
                public ParallelExecution and()
                {
                    return parallelExecution;
                }
    
                @Override
                public Stream<R> get()
                {
                    return result.get()
                                 .stream()
                                 .map(future ->
                                 {
                                     try
                                     {
                                         return future.get(ParallelExecutionImpl.this.timeout, ParallelExecutionImpl.this.timeoutTimeUnit);
                                     }
                                     catch (Exception e)
                                     {
                                         ParallelExecutionImpl.this.exceptionHandler.accept(e);
                                         return null;
                                     }
                                 });
                }
    
                @Override
                public ParallelExecutionAndResult<R> handleExceptions()
                {
                    this.get()
                        .count();
                    return this;
                }
    
                @Override
                public ParallelExecutionAndResult<R> consume(Consumer<Stream<R>> resultConsumer)
                {
                    if (resultConsumer != null)
                    {
                        resultConsumer.accept(this.get());
                    }
                    return this;
                }
    
            };
        }
    
        @Override
        public <R> ParallelExecutionAndResult<R> executeOperations(Stream<Runnable> operations)
        {
            return this.executeTasks(Optional.ofNullable(operations)
                                             .orElse(Stream.empty())
                                             .map(operation -> () ->
                                             {
                                                 operation.run();
                                                 return null;
                                             }));
        }
    
        @Override
        public <R> ParallelExecutionAndResult<R> executeOperation(Runnable operation)
        {
            return this.executeOperations(Stream.of(operation));
        }
    
        @Override
        public AsynchronousParallelExecution asynchronously()
        {
            this.asynchronousExecution = true;
            return this;
        }
    }
}
