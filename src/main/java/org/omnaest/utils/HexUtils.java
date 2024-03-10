package org.omnaest.utils;

import java.nio.ByteBuffer;

import jakarta.xml.bind.DatatypeConverter;

public class HexUtils
{
    public static String toHex(long value)
    {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        byte[] bytes = ArrayUtils.subArrayStartingFromMatching(byteValue -> byteValue != 0, buffer.array());
        return toHex(bytes);
    }

    public static String toHex(byte[] bytes)
    {
        return DatatypeConverter.printHexBinary(bytes);
    }

    public static byte[] toBytes(String hex)
    {
        return DatatypeConverter.parseHexBinary(hex);
    }
}
