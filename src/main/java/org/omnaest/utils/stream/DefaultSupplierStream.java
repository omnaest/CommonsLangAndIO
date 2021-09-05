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
package org.omnaest.utils.stream;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.omnaest.utils.StreamUtils;

/**
 * @see SupplierStream
 * @see StreamUtils#fromSupplier(Supplier)
 * @author omnaest
 * @param <E>
 */
public class DefaultSupplierStream<E> extends StreamDecorator<E> implements SupplierStream<E>
{
    private final Predicate<E> defaultTerminationMatcher = e -> false;
    private Predicate<E>       terminationMatcher        = this.defaultTerminationMatcher;
    private boolean            exclusive                 = true;

    public DefaultSupplierStream(Supplier<E> supplier)
    {
        super(null);

        this.modifyStream(stream -> StreamUtils.fromIterator(new Iterator<E>()
        {
            private AtomicReference<E> takenElement = new AtomicReference<>();
            private AtomicBoolean      terminated   = new AtomicBoolean();

            @Override
            public boolean hasNext()
            {
                boolean previouslyTerminated = this.terminated.get();
                if (previouslyTerminated)
                {
                    return false;
                }
                else
                {
                    this.takeOneElement();
                    boolean notTerminated = Optional.ofNullable(DefaultSupplierStream.this.terminationMatcher)
                                                    .orElse(DefaultSupplierStream.this.defaultTerminationMatcher)
                                                    .negate()
                                                    .test(this.takenElement.get());

                    this.terminated.compareAndSet(false, !notTerminated);

                    return !DefaultSupplierStream.this.exclusive || notTerminated;
                }
            }

            @Override
            public E next()
            {
                this.takeOneElement();
                return this.takenElement.getAndSet(null);
            }

            private void takeOneElement()
            {
                this.takenElement.getAndUpdate(e -> e != null ? e : supplier.get());
            }
        }));

    }

    @Override
    public SupplierStream<E> withTerminationMatcher(Predicate<E> terminationMatcher)
    {
        this.terminationMatcher = terminationMatcher;
        return this;
    }

    @Override
    public SupplierStream<E> withTerminationMatcherInclusive(Predicate<E> terminationMatcher)
    {
        this.terminationMatcher = terminationMatcher;
        this.exclusive = false;
        return this;
    }

}
