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
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.DurationProgressCounter;
import org.omnaest.utils.counter.ImmutableDurationProgressCounter.DurationProgressConsumer;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.element.tri.TriElement;
import org.omnaest.utils.functional.TriConsumer;

/**
 * Helper for {@link Consumer} instances
 * 
 * @author omnaest
 */
public class ConsumerUtils
{
    /**
     * Returns a {@link Consumer} which does accept the given element once. If it accepts the {@link Consumer#accept(Object)} method of the given
     * {@link Consumer} is called, otherwise not.
     * 
     * @param consumer
     * @return
     */
    public static <E> Consumer<E> consumeOnce(Consumer<E> consumer)
    {
        return new Consumer<E>()
        {
            private AtomicBoolean done = new AtomicBoolean(false);

            @Override
            public void accept(E t)
            {
                if (!this.done.getAndSet(true))
                {
                    consumer.accept(t);
                }
            }
        };
    }

    /**
     * {@link Consumer}/{@link BiConsumer} which does nothing
     * 
     * @return
     */
    public static <E, E1, E2> NoOperationConsumer<E, E1, E2> noOperation()
    {
        return new NoOperationConsumer<E, E1, E2>()
        {
            @Override
            public void accept(E t)
            {
                // no operation
            }

            @Override
            public void accept(E1 t, E2 u)
            {
                // no operation
            }
        };
    }

    public static interface NoOperationConsumer<E, E1, E2> extends Consumer<E>, BiConsumer<E1, E2>
    {
    }

    /**
     * Creates a {@link Consumer} which hosts a {@link DurationProgressCounter} that increments for each call to {@link Consumer#accept(Object)}. For all
     * 'modulo' operations the given {@link DurationProgressConsumer} is called.
     * 
     * @see DurationProgressCounter#ifModulo(int, DurationProgressConsumer)
     * @param modulo
     * @param maximum
     * @param durationProgressConsumer
     * @return
     */
    public static <E> Consumer<E> progessCounter(int modulo, long maximum, DurationProgressConsumer durationProgressConsumer)
    {
        DurationProgressCounter progressCounter = Counter.fromZero()
                                                         .asDurationProgressCounter()
                                                         .withMaximum(maximum);
        return element -> progressCounter.increment()
                                         .ifModulo(modulo, durationProgressConsumer);
    }

    public static <E> void consumeWith(E element, Optional<Consumer<E>> elementConsumer)
    {
        consumeWithAndGet(element, elementConsumer);
    }

    public static <E> E consumeWithAndGet(E element, Optional<Consumer<E>> elementConsumer)
    {
        if (elementConsumer != null)
        {
            elementConsumer.ifPresent(consumer -> consumer.accept(element));
        }
        return element;
    }

    public static <E> void consumeWith(E element, Consumer<E> elementConsumer)
    {
        if (elementConsumer != null)
        {
            elementConsumer.accept(element);
        }
    }

    public static <E> E consumeWithAndGet(E element, Consumer<? super E> elementConsumer)
    {
        consumeWith(element, elementConsumer);
        return element;
    }

    public static <E1, E2> void consumeWith(E1 element1, E2 element2, BiConsumer<? super E1, ? super E2> elementConsumer)
    {
        if (elementConsumer != null)
        {
            elementConsumer.accept(element1, element2);
        }
    }

    public static <E1, E2> BiElement<E1, E2> consumeWithAndGet(E1 element1, E2 element2, BiConsumer<? super E1, ? super E2> elementConsumer)
    {
        consumeWith(element1, element2, elementConsumer);
        return BiElement.of(element1, element2);
    }

    public static <E1, E2, E3> void consumeWith(E1 element1, E2 element2, E3 element3, TriConsumer<? super E1, ? super E2, ? super E3> elementConsumer)
    {
        if (elementConsumer != null)
        {
            elementConsumer.accept(element1, element2, element3);
        }
    }

    public static <E1, E2, E3> TriElement<E1, E2, E3> consumeWithAndGet(E1 element1, E2 element2, E3 element3,
                                                                        TriConsumer<? super E1, ? super E2, ? super E3> elementConsumer)
    {
        consumeWith(element1, element2, element3, elementConsumer);
        return TriElement.of(element1, element2, element3);
    }

    /**
     * Returns a {@link ListAddingConsumer} instance
     * 
     * @return
     */
    public static <E> ListAddingConsumer<E> newAddingConsumer()
    {
        return new ListAddingConsumer<E>()
        {
            private List<E> elements = new ArrayList<>();

            @Override
            public void accept(E element)
            {
                this.elements.add(element);
            }

            @Override
            public List<E> get()
            {
                return Collections.unmodifiableList(this.elements);
            }
        };
    }

    public static <E1, E2> ListAddingBiConsumer<E1, E2> newAddingBiConsumer()
    {
        return new ListAddingBiConsumer<>()
        {
            private List<BiElement<E1, E2>> elements = new ArrayList<>();

            @Override
            public void accept(E1 element1, E2 element2)
            {
                this.elements.add(BiElement.of(element1, element2));
            }

            @Override
            public List<BiElement<E1, E2>> get()
            {
                return Collections.unmodifiableList(this.elements);
            }
        };
    }

    public static <E1, E2, E3> ListAddingTriConsumer<E1, E2, E3> newAddingTriConsumer()
    {
        return new ListAddingTriConsumer<>()
        {
            private List<TriElement<E1, E2, E3>> elements = new ArrayList<>();

            @Override
            public void accept(E1 element1, E2 element2, E3 element3)
            {
                this.elements.add(TriElement.of(element1, element2, element3));
            }

            @Override
            public List<TriElement<E1, E2, E3>> get()
            {
                return Collections.unmodifiableList(this.elements);
            }
        };
    }

    /**
     * A {@link ListAddingConsumer} consumes element by element and appends it to an internal {@link List}.
     * 
     * @see #get() returns an immutable instance of the consumed elements
     * @author omnaest
     * @param <E>
     */
    public static interface ListAddingConsumer<E> extends Consumer<E>, Supplier<List<E>>
    {

    }

    /**
     * Similar to {@link ListAddingConsumer} but captures a {@link BiConsumer} invocation as a {@link BiElement}.
     * 
     * @author omnaest
     * @param <E1>
     * @param <E2>
     */
    public static interface ListAddingBiConsumer<E1, E2> extends BiConsumer<E1, E2>, Supplier<List<BiElement<E1, E2>>>
    {

    }

    /**
     * Similar to {@link ListAddingConsumer} but captures a {@link TriConsumer} invocation as a {@link TriElement}.
     * 
     * @author omnaest
     * @param <E1>
     * @param <E2>
     * @param <E3>
     */
    public static interface ListAddingTriConsumer<E1, E2, E3> extends TriConsumer<E1, E2, E3>, Supplier<List<TriElement<E1, E2, E3>>>
    {

    }

    /**
     * Throws a {@link RuntimeException} based on the given exception mapper result.
     * 
     * @param exceptionMapper
     * @return
     */
    public static <E> Consumer<E> throwException(Function<E, RuntimeException> exceptionMapper)
    {
        return element ->
        {
            RuntimeException exception = exceptionMapper.apply(element);
            throw exception;
        };
    }
}
