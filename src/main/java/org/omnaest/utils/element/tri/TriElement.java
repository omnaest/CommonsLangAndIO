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
package org.omnaest.utils.element.tri;

import org.omnaest.utils.element.bi.BiElement;

/**
 * Representation of a tuple of three elements
 * 
 * @see BiElement
 * @author omnaest
 * @param <E1>
 * @param <E2>
 * @param <E3>
 */
public interface TriElement<E1, E2, E3>
{
    public E1 getFirst();

    public E2 getSecond();

    public E3 getThird();

    public BiElement<E1, E2> getFirstAndSecond();

    public BiElement<E1, E3> getFirstAndThird();

    public BiElement<E2, E3> getSecondAndThird();

    /**
     * Creates a new {@link TriElement} instance
     * 
     * @param first
     * @param second
     * @param third
     * @return
     */
    public static <E1, E2, E3> TriElement<E1, E2, E3> of(E1 first, E2 second, E3 third)
    {
        return new DefaultTriElement<E1, E2, E3>(first, second, third);
    }

    /**
     * Returns an empty {@link TriElement}
     * 
     * @return
     */
    public static <R1, R2, R3> TriElement<R1, R2, R3> empty()
    {
        return TriElement.of(null, null, null);
    }

}
