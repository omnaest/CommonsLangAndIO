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
package org.omnaest.utils.functional;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Similar to {@link Supplier}
 * 
 * @author omnaest
 * @param <E>
 */
public interface Provider<E> extends Supplier<E>
{

    /**
     * Allows to chain a mapper {@link Function} to the current provider
     * 
     * @param mapper
     * @return
     */
    public default <R> Provider<R> and(Function<E, R> mapper)
    {
        return () -> mapper.apply(get());
    }
}
