package org.omnaest.utils.file.storage;

public interface ByteArrayBlockFileStorage extends BlockFileStorage<byte[]>
{

    StringBlockFileStorage asStringBlockFileStorage();

}
