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
package org.omnaest.utils.element.cached;

import java.util.Map;
import java.util.function.Function;

/**
 * {@link Function} wrapper which is cached by a {@link Map}
 * 
 * @author omnaest
 * @param <T>
 * @param <R>
 */
public interface CachedFunction<T, R> extends Function<T, R>
{
    /**
     * Creates a new {@link CachedFunction} based on a given {@link Function} and a {@link Map}
     * 
     * @param function
     * @param map
     * @return
     */
    public static <T, R> CachedFunction<T, R> of(Function<T, R> function, Map<T, R> map)
    {
        return new MapCachedFunction<>(map, function);
    }
}
