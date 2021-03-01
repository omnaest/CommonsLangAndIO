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

import org.omnaest.utils.element.bi.UnaryBiElement;
import org.omnaest.utils.element.multi.UnaryElements;

/**
 * @see #of(Object, Object, Object)
 * @see TriElement
 * @see UnaryElements
 * @author omnaest
 * @param <E>
 */
public interface UnaryTriElement<E> extends TriElement<E, E, E>
{

    @Override
    public UnaryBiElement<E> getFirstAndSecond();

    @Override
    public UnaryBiElement<E> getFirstAndThird();

    @Override
    public UnaryBiElement<E> getSecondAndThird();

    /**
     * Creates a new {@link UnaryTriElement} instance
     * 
     * @param first
     * @param second
     * @param third
     * @return
     */
    public static <E> UnaryTriElement<E> of(E first, E second, E third)
    {
        return new DefaultUnaryTriElement<E>(first, second, third);
    }
}
