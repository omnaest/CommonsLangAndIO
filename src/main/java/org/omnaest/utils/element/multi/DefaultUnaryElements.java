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

import java.util.Arrays;

import org.omnaest.utils.element.bi.UnaryBiElement;

/**
 * @see UnaryElements
 * @author omnaest
 * @param <E>
 */
public class DefaultUnaryElements<E> implements UnaryElements<E>
{
    private E[] values;

    @SafeVarargs
    public DefaultUnaryElements(E... values)
    {
        super();
        this.values = values;
    }

    @Override
    public UnaryBiElement<E> getFirstAndSecond()
    {
        return UnaryBiElement.of(this.getFirst(), this.getSecond());
    }

    @Override
    public UnaryBiElement<E> getFirstAndThird()
    {
        return UnaryBiElement.of(this.getFirst(), this.getThird());
    }

    @Override
    public UnaryBiElement<E> getSecondAndThird()
    {
        return UnaryBiElement.of(this.getSecond(), this.getThird());
    }

    @Override
    public E getFirst()
    {
        return this.getNTh(1);
    }

    @Override
    public E getSecond()
    {
        return this.getNTh(2);
    }

    @Override
    public E getThird()
    {
        return this.getNTh(3);
    }

    @Override
    public E getFourth()
    {
        return this.getNTh(4);
    }

    @Override
    public E getFifth()
    {
        return this.getNTh(5);
    }

    @Override
    public E getNTh(int index)
    {
        return this.values.length >= index ? this.values[index - 1] : null;
    }

    @Override
    public String toString()
    {
        return "DefaultUnaryElements [values=" + Arrays.toString(this.values) + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.values);
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        DefaultUnaryElements other = (DefaultUnaryElements) obj;
        if (!Arrays.equals(this.values, other.values))
        {
            return false;
        }
        return true;
    }

}
