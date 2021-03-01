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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Filters the first tested element ( this{@link #test(Object)} returns false ) and captures it, so that it can be retrieved via {@link #get()}
 * <br>
 * <br>
 * A {@link FirstElementFilterCapture} can be used in combination with {@link Stream#filter(Predicate)}
 * 
 * @author omnaest
 * @param <E>
 */
public class FirstElementFilterCapture<E> implements Predicate<E>, Supplier<E>
{
    private AtomicReference<E> element  = new AtomicReference<>();
    private AtomicBoolean      captured = new AtomicBoolean();

    @Override
    public boolean test(E element)
    {
        boolean updated = this.captured.compareAndSet(false, true);
        if (updated)
        {
            this.element.set(element);
        }
        return !updated;
    }

    @Override
    public E get()
    {
        return this.element.get();
    }
}
