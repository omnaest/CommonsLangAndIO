package org.omnaest.utils.element.bi;

/**
 * @see BiElement#of(Object, Object)
 * @author omnaest
 * @param <E1>
 * @param <E2>
 */
public class DefaultBiElement<E1, E2> implements BiElement<E1, E2>
{
    private E1 element1;
    private E2 element2;

    DefaultBiElement(E1 element1, E2 element2)
    {
        this.element1 = element1;
        this.element2 = element2;
    }

    @Override
    public E1 getFirst()
    {
        return this.element1;
    }

    @Override
    public E2 getSecond()
    {
        return this.element2;
    }

    @Override
    public String toString()
    {
        return "DefaultBiElement [element1=" + this.element1 + ", element2=" + this.element2 + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.element1 == null) ? 0 : this.element1.hashCode());
        result = prime * result + ((this.element2 == null) ? 0 : this.element2.hashCode());
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
        if (this.element1 == null)
        {
            if (other.element1 != null)
            {
                return false;
            }
        }
        else if (!this.element1.equals(other.element1))
        {
            return false;
        }
        if (this.element2 == null)
        {
            if (other.element2 != null)
            {
                return false;
            }
        }
        else if (!this.element2.equals(other.element2))
        {
            return false;
        }
        return true;
    }

}