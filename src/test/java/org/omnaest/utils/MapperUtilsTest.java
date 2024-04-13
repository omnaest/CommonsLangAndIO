package org.omnaest.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class MapperUtilsTest
{

    @Test
    public void testCastToListType()
    {
        assertEquals(List.of("1"), Optional.of(List.of("1"))
                                           .map(MapperUtils.castToListType(String.class))
                                           .get());
    }

    @Test
    public void testCastToSetType()
    {
        assertEquals(Set.of("1"), Optional.of(Set.of("1"))
                                          .map(MapperUtils.castToSetType(String.class))
                                          .get());
    }

    @Test
    public void testCastToListTypeSilently()
    {
        assertEquals(List.of("1"), Optional.of(List.of("1"))
                                           .map(MapperUtils.castToListTypeSilently(String.class))
                                           .get());
        assertEquals(List.of("1"), Optional.of(Set.of("2"))
                                           .map(MapperUtils.castToListTypeSilently(String.class))
                                           .orElse(List.of("1")));
    }

    @Test
    public void testCastToSetTypeSilently()
    {
        assertEquals(Set.of("1"), Optional.of(Set.of("1"))
                                          .map(MapperUtils.castToSetTypeSilently(String.class))
                                          .get());
        assertEquals(Set.of("1"), Optional.of(List.of("2"))
                                          .map(MapperUtils.castToSetTypeSilently(String.class))
                                          .orElse(Set.of("1")));
    }

    @Test
    public void testCastToType()
    {
        assertEquals("1", Optional.of("1")
                                  .map(MapperUtils.castToType(String.class))
                                  .get());
    }

    @Test
    public void testCastToTypeSilently()
    {
        assertEquals("1", Optional.of("1")
                                  .map(MapperUtils.castToTypeSilently(String.class))
                                  .get());
        assertEquals("1", Optional.of(Long.valueOf(2))
                                  .map(MapperUtils.castToTypeSilently(String.class))
                                  .orElse("1"));
    }

}
