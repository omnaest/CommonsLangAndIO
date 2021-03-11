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

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Similar to {@link Iterable} but provides additionally the {@link #stream()} method which is to be implemented.
 * 
 * @see Stream
 * @author omnaest
 * @param <E>
 */
public interface Streamable<E> extends Iterable<E>
{
    public Stream<E> stream();

    /**
     * Default implementation based on the {@link #stream()} method and the {@link Stream#iterator()}.
     * 
     * @return
     */
    @Override
    public default Iterator<E> iterator()
    {
        return this.stream()
                   .iterator();
    }
}