package org.omnaest.utils.element.tri;

import org.omnaest.utils.element.bi.BiElement;

public class DefaultTriElement<E1, E2, E3> implements TriElement<E1, E2, E3>
{
    protected E1 first;
    protected E2 second;
    protected E3 third;

    DefaultTriElement(E1 first, E2 second, E3 third)
    {
        super();
        this.first = first;
        this.second = second;
        this.third = third;
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
    public E3 getThird()
    {
        return this.third;
    }

    @Override
    public BiElement<E1, E2> getFirstAndSecond()
    {
        return BiElement.of(this.first, this.second);
    }

    @Override
    public BiElement<E1, E3> getFirstAndThird()
    {
        return BiElement.of(this.first, this.third);
    }

    @Override
    public BiElement<E2, E3> getSecondAndThird()
    {
        return BiElement.of(this.second, this.third);
    }

    @Override
    public String toString()
    {
        return "DefaultTriElement [first=" + this.first + ", second=" + this.second + ", third=" + this.third + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.first == null) ? 0 : this.first.hashCode());
        result = prime * result + ((this.second == null) ? 0 : this.second.hashCode());
        result = prime * result + ((this.third == null) ? 0 : this.third.hashCode());
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
        DefaultTriElement other = (DefaultTriElement) obj;
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
        if (this.third == null)
        {
            if (other.third != null)
            {
                return false;
            }
        }
        else if (!this.third.equals(other.third))
        {
            return false;
        }
        return true;
    }

}
