package org.omnaest.utils.element.tri;

import org.omnaest.utils.element.bi.UnaryBiElement;

public class DefaultUnaryTriElement<E> implements UnaryTriElement<E>
{
    protected E first;
    protected E second;
    protected E third;

    DefaultUnaryTriElement(E first, E second, E third)
    {
        super();
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public E getFirst()
    {
        return this.first;
    }

    @Override
    public E getSecond()
    {
        return this.second;
    }

    @Override
    public E getThird()
    {
        return this.third;
    }

    @Override
    public UnaryBiElement<E> getFirstAndSecond()
    {
        return UnaryBiElement.of(this.first, this.second);
    }

    @Override
    public UnaryBiElement<E> getFirstAndThird()
    {
        return UnaryBiElement.of(this.first, this.third);
    }

    @Override
    public UnaryBiElement<E> getSecondAndThird()
    {
        return UnaryBiElement.of(this.second, this.third);
    }

    @Override
    public String toString()
    {
        return "DefaultUnaryTriElement [first=" + this.first + ", second=" + this.second + ", third=" + this.third + "]";
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
        DefaultUnaryTriElement other = (DefaultUnaryTriElement) obj;
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
