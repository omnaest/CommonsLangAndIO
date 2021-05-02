package org.omnaest.utils.file.storage;

public interface BlockFileStorage<E>
{
    public E read(int rowIndex);

    public void write(int rowIndex, E data);
}
