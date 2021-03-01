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
package org.omnaest.utils.element.multi;

import org.omnaest.utils.element.bi.UnaryBiElement;
import org.omnaest.utils.element.tri.UnaryTriElement;

/**
 * Wrapper around multiple elements of the same type.
 * 
 * @see #of(Object...)
 * @see UnaryBiElement
 * @see UnaryTriElement
 * @author omnaest
 * @param <E>
 */
public interface UnaryElements<E> extends UnaryTriElement<E>
{
    /**
     * Returns the n-th value. index = 1,2,3, ...
     * 
     * @param index
     * @return
     */
    public E getNTh(int index);

    public E getFourth();

    public E getFifth();

    /**
     * Creates a new {@link UnaryElements} instance with the given values
     * 
     * @param values
     * @return
     */
    @SafeVarargs
    public static <E> UnaryElements<E> of(E... values)
    {
        return new DefaultUnaryElements<>(values);
    }
}
