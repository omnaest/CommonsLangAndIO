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
package org.omnaest.utils.element.bi;

import java.util.List;
import java.util.stream.Stream;

/**
 * {@link BiElement} with two elements of the same type
 * 
 * @author omnaest
 * @param <E>
 */
public interface UnaryBiElement<E> extends BiElement<E, E>
{
    /**
     * Returns a non modifiable {@link List} for the given elements
     * 
     * @see #asStream()
     * @return
     */
    public List<E> asList();

    /**
     * Returns a {@link Stream} wrapping the {@link #getFirst()} and {@link #getSecond()} element. The elements of this {@link Stream} might be null.
     * 
     * @see #asList()
     * @see #asNonNullStream()
     * @return
     */
    public default Stream<E> asStream()
    {
        return Stream.of(this.getFirst(), this.getSecond());
    }

    /**
     * Similar to {@link #asStream()} but does not contain any null values.
     * 
     * @see #asStream()
     * @return
     */
    public default Stream<E> asNonNullStream()
    {
        return this.asStream()
                   .filter(value -> value != null);
    }

    /**
     * Returns a new {@link UnaryBiElement} for the given elements
     * 
     * @param element1
     * @param element2
     * @return
     */
    public static <E> UnaryBiElement<E> of(E element1, E element2)
    {
        return new DefaultUnaryBiElement<E>(element1, element2);
    }

    /**
     * Returns true, if all values are {@link #equals(Object)}
     * 
     * @return
     */
    public boolean hasEqualValues();
}
