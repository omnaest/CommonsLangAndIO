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
package org.omnaest.utils.element.lar;

import org.omnaest.utils.element.bi.BiElement;

/**
 * @see UnaryLeftAndRight
 * @see IntegerLeftAndRight
 * @see ModifiableUnaryLeftAndRight
 * @author omnaest
 * @param <E>
 */
public class LeftAndRight<L, R>
{
    protected L left;
    protected R right;

    public LeftAndRight(L left, R right)
    {
        super();
        this.left = left;
        this.right = right;
    }

    public L getLeft()
    {
        return this.left;
    }

    public R getRight()
    {
        return this.right;
    }

    public boolean hasLeft()
    {
        return this.left != null;
    }

    public boolean hasRight()
    {
        return this.right != null;
    }

    public boolean hasAny()
    {
        return this.hasLeft() || this.hasRight();
    }

    public boolean hasBoth()
    {
        return this.hasLeft() && this.hasRight();
    }

    @Override
    public String toString()
    {
        return "LeftAndRight [left=" + this.left + ", right=" + this.right + "]";
    }

    protected LeftAndRight<L, R> setLeft(L left)
    {
        this.left = left;
        return this;
    }

    protected LeftAndRight<L, R> setRight(R right)
    {
        this.right = right;
        return this;
    }

    public BiElement<L, R> asBiElement()
    {
        return BiElement.of(this.left, this.right);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.left == null) ? 0 : this.left.hashCode());
        result = prime * result + ((this.right == null) ? 0 : this.right.hashCode());
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
        LeftAndRight other = (LeftAndRight) obj;
        if (this.left == null)
        {
            if (other.left != null)
            {
                return false;
            }
        }
        else if (!this.left.equals(other.left))
        {
            return false;
        }
        if (this.right == null)
        {
            if (other.right != null)
            {
                return false;
            }
        }
        else if (!this.right.equals(other.right))
        {
            return false;
        }
        return true;
    }
}