package org.omnaest.utils;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Helper around {@link Collector}s
 * 
 * @author omnaest
 */
public class CollectorUtils
{
    public static <K, V, VR> Collector<Map.Entry<K, V>, ?, Map<K, VR>> toValueMappedMap(Function<Map.Entry<K, V>, VR> valueMapper)
    {
        return Collectors.toMap(entry -> entry.getKey(), valueMapper);
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> appendToMap(TreeMap<K, V> treeMap)
    {
        return Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), (e1, e2) -> e1, () -> treeMap);
    }
}
