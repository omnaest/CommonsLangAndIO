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
package org.omnaest.utils.supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Test;

public class IteratorToOptionalSupplierAdapterTest
{

    @Test
    public void testGet() throws Exception
    {
        Iterator<String> iterator = Arrays.asList("a", "b", "c")
                                          .iterator();
        Supplier<Optional<String>> supplier = OptionalSupplier.of(iterator);

        {
            Optional<String> optional = supplier.get();
            assertTrue(optional.isPresent());
            assertEquals("a", optional.get());
        }
        {
            Optional<String> optional = supplier.get();
            assertTrue(optional.isPresent());
            assertEquals("b", optional.get());
        }
        {
            Optional<String> optional = supplier.get();
            assertTrue(optional.isPresent());
            assertEquals("c", optional.get());
        }
        {
            Optional<String> optional = supplier.get();
            assertFalse(optional.isPresent());
        }
    }

}
