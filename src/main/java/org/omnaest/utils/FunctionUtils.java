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

import java.util.function.Function;
import java.util.function.Supplier;

import org.omnaest.utils.functional.UnaryBiFunction;

/**
 * Helper methods around {@link Function} logic
 * 
 * @author omnaest
 */
public class FunctionUtils
{
    /**
     * Returns a {@link Supplier} that throws the {@link RuntimeException} provided by the given {@link Supplier} if the {@link Supplier#get()} method is
     * called.
     * 
     * @param exceptionSupplier
     * @return
     */
    public static <R> UnaryBiFunction<R> toExceptionThrowingSupplier(Supplier<? extends RuntimeException> exceptionSupplier)
    {
        return (a, b) ->
        {
            throw exceptionSupplier.get();
        };
    }
}
