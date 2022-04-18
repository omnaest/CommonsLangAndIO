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

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import org.junit.Test;

/**
 * @see SupplierUtils
 * @author omnaest
 */
public class SupplierUtilsTest
{

    @Test
    public void testToSoftReferenceCached() throws Exception
    {
        Iterator<String> iterator = Arrays.asList("a", "b")
                                          .iterator();
        Supplier<String> supplier = SupplierUtils.toSoftReferenceCached(() -> iterator.next());
        assertEquals("a", supplier.get());
        assertEquals("a", supplier.get());
    }

    @Test
    public void testToChainableSupplier() throws Exception
    {
        assertEquals("a", SupplierUtils.toChainableSupplier(() -> "a")
                                       .get());
        assertEquals("ab", SupplierUtils.toChainableSupplier(() -> "a")
                                        .andThen(a -> a + "b")
                                        .get());
    }

}
