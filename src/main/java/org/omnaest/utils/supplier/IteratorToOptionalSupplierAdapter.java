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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * {@link OptionalSupplier} which is based on an {@link Iterator}
 * 
 * @author omnaest
 * @param <E>
 */
public class IteratorToOptionalSupplierAdapter<E> implements OptionalSupplier<E>
{
    private Iterator<E> iterator;

    public IteratorToOptionalSupplierAdapter(Iterator<E> iterator)
    {
        super();
        this.iterator = iterator;
    }

    @Override
    public Optional<E> get()
    {
        E value = null;
        if (this.iterator.hasNext())
        {
            try
            {
                value = this.iterator.next();
            }
            catch (NoSuchElementException e)
            {
                //do nothing
            }
        }
        return Optional.ofNullable(value);
    }

}
