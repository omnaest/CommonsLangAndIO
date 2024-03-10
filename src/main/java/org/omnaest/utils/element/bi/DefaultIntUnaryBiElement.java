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

import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @see UnaryBiElement#of(Object, Object)
 * @author omnaest
 * @param <E>
 */
@XmlRootElement
public class DefaultIntUnaryBiElement implements IntUnaryBiElement
{
    private int element1;
    private int element2;

    DefaultIntUnaryBiElement(int element1, int element2)
    {
        this.element1 = element1;
        this.element2 = element2;
    }

    DefaultIntUnaryBiElement()
    {
        super();
    }

    @Override
    public int getFirst()
    {
        return this.element1;
    }

    @Override
    public int getSecond()
    {
        return this.element2;
    }

    public List<Integer> asList()
    {
        return Arrays.asList(this.element1, this.element2);
    }

    @Override
    public String toString()
    {
        return "DefaultUnaryBiElement [element1=" + this.element1 + ", element2=" + this.element2 + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.element1;
        result = prime * result + this.element2;
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
        DefaultIntUnaryBiElement other = (DefaultIntUnaryBiElement) obj;
        if (this.element1 != other.element1)
        {
            return false;
        }
        if (this.element2 != other.element2)
        {
            return false;
        }
        return true;
    }

    public boolean hasEqualValues()
    {
        return this.element1 == this.element2;
    }

}
