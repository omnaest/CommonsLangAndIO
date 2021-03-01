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
import java.util.Optional;
import java.util.function.Function;

public class MapCachedFunction<T, R> implements CachedFunction<T, R>
{
    private Map<T, R>      map;
    private Function<T, R> function;

    public MapCachedFunction(Map<T, R> map, Function<T, R> function)
    {
        super();
        this.map = map;
        this.function = function;
    }

    @Override
    public R apply(T t)
    {
        return Optional.ofNullable(t)
                       .map(v -> this.map.computeIfAbsent(v, this.function))
                       .orElseGet(() -> this.function.apply(t));
    }

}
