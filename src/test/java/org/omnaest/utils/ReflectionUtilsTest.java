/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
