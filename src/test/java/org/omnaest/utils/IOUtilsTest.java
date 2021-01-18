package org.omnaest.utils;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class IOUtilsTest
{

    @Test
    public void testCopyWithProgess() throws Exception
    {
        List<Double> progressLog = new ArrayList<>();
        byte[] data = IntStream.range(0, 10000)
                               .mapToObj(i -> "x" + i)
                               .collect(Collectors.joining())
                               .getBytes();
        IOUtils.copyWithProgess(new ByteArrayInputStream(data), new ByteArrayOutputStream(), progressLog::add);
        assertTrue(progressLog.size() >= 2);
        assertTrue(progressLog.stream()
                              .mapToDouble(v -> v)
                              .max()
                              .getAsDouble() <= 1.00001);
        assertTrue(progressLog.stream()
                              .mapToDouble(v -> v)
                              .min()
                              .getAsDouble() >= 0.0);
    }

}
