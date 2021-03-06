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

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link Stream} which is based on a given {@link Supplier}
 * 
 * @see #withTerminationMatcher(Predicate)
 * @see #withTerminationMatcherInclusive(Predicate)
 * @author omnaest
 * @param <E>
 */
public interface SupplierStream<E> extends Stream<E>
{
    /**
     * Terminates the {@link Stream} if the {@link Predicate#test(Object)} is true excluding the matched element
     * 
     * @param terminationMatcher
     * @return
     */
    public SupplierStream<E> withTerminationMatcher(Predicate<E> terminationMatcher);

    /**
     * Terminates the {@link Stream} if the {@link Predicate#test(Object)} is true including the matched element
     * 
     * @param terminationMatcher
     * @return
     */
    public SupplierStream<E> withTerminationMatcherInclusive(Predicate<E> terminationMatcher);
}
