package org.omnaest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.omnaest.utils.ReflectionUtils.FieldReflection;

public class ReflectionUtilsTest
{

    @Test
    public void testNewInstance() throws Exception
    {
        assertEquals(Arrays.asList("a", "b"), ReflectionUtils.newInstance(ArrayList.class, Arrays.asList("a", "b")));
    }

    @Test
    public void testOfField() throws Exception
    {
        FieldReflection fieldReflection = ReflectionUtils.of(ReflectionUtils.of(TestDomain.class)
                                                                            .getField("field1")
                                                                            .get()
                                                                            .getRawField());
        TestAnnotation testAnnotation = fieldReflection.getAnnotations(TestAnnotation.class)
                                                       .findFirst()
                                                       .get();
        assertNotNull(testAnnotation);
    }

    protected static class TestDomain
    {
        @TestAnnotation
        private String field1;
    }

    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface TestAnnotation
    {

    }
}
