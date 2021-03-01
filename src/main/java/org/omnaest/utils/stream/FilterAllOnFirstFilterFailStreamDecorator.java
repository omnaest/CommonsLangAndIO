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
package org.omnaest.utils.stream;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilterAllOnFirstFilterFailStreamDecorator<E> extends StreamDecorator<E>
{
    private AtomicBoolean terminate = new AtomicBoolean(false);

    public FilterAllOnFirstFilterFailStreamDecorator(Stream<E> stream)
    {
        super(stream);
    }

    @Override
    public Stream<E> filter(Predicate<? super E> predicate)
    {
        return super.filter(element ->
        {
            boolean result = !this.terminate.get() && predicate.test(element);
            this.terminate.set(!result);
            return result;
        });
    }

    @Override
    public String toString()
    {
        return "FilterAllOnFirstFilterFailStreamDecorator [terminate=" + this.terminate + "]";
    }

}
