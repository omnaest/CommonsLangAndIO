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

/**
 * @see BiElement#of(Object, Object)
 * @author omnaest
 * @param <E1>
 * @param <E2>
 */
public class DefaultBiElement<E1, E2> implements BiElement<E1, E2>
{
    private E1 first;
    private E2 second;

    DefaultBiElement(E1 first, E2 second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public E1 getFirst()
    {
        return this.first;
    }

    @Override
    public E2 getSecond()
    {
        return this.second;
    }

    @Override
    public String toString()
    {
        return "[" + this.first + ", " + this.second + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.first == null) ? 0 : this.first.hashCode());
        result = prime * result + ((this.second == null) ? 0 : this.second.hashCode());
        return result;
    }

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
        @SuppressWarnings("rawtypes")
        DefaultBiElement other = (DefaultBiElement) obj;
        if (this.first == null)
        {
            if (other.first != null)
            {
                return false;
            }
        }
        else if (!this.first.equals(other.first))
        {
            return false;
        }
        if (this.second == null)
        {
            if (other.second != null)
            {
                return false;
            }
        }
        else if (!this.second.equals(other.second))
        {
            return false;
        }
        return true;
    }

}
