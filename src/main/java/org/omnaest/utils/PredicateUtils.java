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
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.omnaest.utils.stream.FirstElementFilterCapture;

/**
 * Utils regarding {@link Predicate} logic
 * 
 * @author omnaest
 */
public class PredicateUtils
{
    /**
     * Returns a {@link Predicate} that returns true if the given test object is not null
     * 
     * @return
     */
    public static <E> Predicate<E> notNull()
    {
        return Objects::nonNull;
    }

    public static interface ModuloPredicateBuilder<E>
    {
        public Predicate<E> equals(int value);

        public Predicate<E> equalsZero();
    }

    public static <E> ModuloPredicateBuilder<E> modulo(int modulo)
    {
        return new ModuloPredicateBuilder<E>()
        {
            @Override
            public Predicate<E> equals(int value)
            {
                return new Predicate<E>()
                {
                    private AtomicLong counter = new AtomicLong();

                    @Override
                    public boolean test(E t)
                    {
                        return this.counter.getAndIncrement() % modulo == value;
                    }
                };
            }

            @Override
            public Predicate<E> equalsZero()
            {
                return this.equals(0);
            }
        };
    }

    /**
     * Returns a {@link FirstElementFilterCapture} which can be used in combination with {@link Stream#filter(Predicate)}
     * 
     * @return
     */
    public static <E> FirstElementFilterCapture<E> firstElementFilterCapture()
    {
        return new FirstElementFilterCapture<>();
    }

    /**
     * @see StringUtils#isNotBlank(CharSequence)
     * @return
     */
    public static Predicate<? super String> notBlank()
    {
        return str -> StringUtils.isNotBlank(str);
    }

    /**
     * @see StringUtils#isNotEmpty(CharSequence)
     * @return
     */
    public static Predicate<? super String> notEmpty()
    {
        return str -> StringUtils.isNotEmpty(str);
    }

    /**
     * Returns true if the tested object is null
     * 
     * @return
     */
    public static <T> Predicate<T> isNull()
    {
        return o -> o == null;
    }

    /**
     * Returns true if the tested element does not match the given
     * 
     * @param gene
     * @return
     */
    public static <T> Predicate<T> notEqueals(T object)
    {
        return t -> !Objects.equals(t, object);
    }

    /**
     * Returns allways true for all elements
     * 
     * @return
     */
    public static <T> Predicate<T> allMatching()
    {
        return t -> true;
    }

}
