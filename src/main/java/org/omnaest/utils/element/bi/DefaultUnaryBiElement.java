package org.omnaest.utils.element.bi;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @see UnaryBiElement#of(Object, Object)
 * @author omnaest
 * @param <E>
 */
@XmlRootElement
public class DefaultUnaryBiElement<E> implements UnaryBiElement<E>
{
    private E element1;
    private E element2;

    DefaultUnaryBiElement(E element1, E element2)
    {
        this.element1 = element1;
        this.element2 = element2;
    }

    DefaultUnaryBiElement()
    {
        super();
    }

    @Override
    public E getFirst()
    {
        return this.element1;
    }

    @Override
    public E getSecond()
    {
        return this.element2;
    }

    @Override
    public List<E> asList()
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
        DefaultUnaryBiElement other = (DefaultUnaryBiElement) obj;
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