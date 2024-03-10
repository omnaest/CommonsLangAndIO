package org.omnaest.utils.list.cyclic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.omnaest.utils.ListUtils;

public class CyclicArrayListTest
{

    @Test
    public void testGetAndSet()
    {
        List<String> list = ListUtils.newCyclicList(3);
        assertEquals(0, list.size());

        list.add("0");
        assertEquals(1, list.size());
        assertEquals("0", list.get(0));
        assertEquals(List.of("0"), list);

        list.add("1");
        assertEquals(2, list.size());
        assertEquals("0", list.get(0));
        assertEquals("1", list.get(1));
        assertEquals(List.of("0", "1"), list);

        list.add("2");
        assertEquals(3, list.size());
        assertEquals("0", list.get(0));
        assertEquals("1", list.get(1));
        assertEquals("2", list.get(2));
        assertEquals(List.of("0", "1", "2"), list);

        list.add("3");
        assertEquals(3, list.size());
        assertEquals("3", list.get(0));
        assertEquals("1", list.get(1));
        assertEquals("2", list.get(2));
        assertEquals(List.of("3", "1", "2"), list);

        list.add("4");
        assertEquals(3, list.size());
        assertEquals("3", list.get(0));
        assertEquals("4", list.get(1));
        assertEquals("2", list.get(2));
        assertEquals(List.of("3", "4", "2"), list);

        list.add("5");
        assertEquals(3, list.size());
        assertEquals("3", list.get(0));
        assertEquals("4", list.get(1));
        assertEquals("5", list.get(2));
        assertEquals(List.of("3", "4", "5"), list);
    }

    @Test
    public void testGetAndSetInFloatingMode()
    {
        List<String> list = ListUtils.newCyclicFloatingList(3);
        assertEquals(0, list.size());

        list.add("0");
        assertEquals(List.of("0"), list);

        list.add("1");
        assertEquals(List.of("0", "1"), list);

        list.add("2");
        assertEquals(List.of("0", "1", "2"), list);

        list.add("3");
        assertEquals(List.of("1", "2", "3"), list);

        list.add("4");
        assertEquals(List.of("2", "3", "4"), list);

        list.add("5");
        assertEquals(List.of("3", "4", "5"), list);
    }

}
