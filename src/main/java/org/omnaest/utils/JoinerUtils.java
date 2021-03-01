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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;

/**
 * Helper to perform joins on {@link Stream}s and {@link Collection}s
 * 
 * @see #join(Collection)
 * @author omnaest
 */
public class JoinerUtils
{
    public static interface Joiner<E>
    {
        public <E2> JoinerWithElements<E, E2> with(Collection<E2> joinElements);

        public <E2> JoinerWithElements<E, E2> with(Stream<E2> joinElements);
    }

    public static interface JoinerWithElements<E1, E2>
    {
        public InnerJoiner<E1, E2> inner();

        public Stream<BiElement<E1, E2>> cartesian();
    }

    public static interface InnerJoiner<E1, E2>
    {
        public <PK> Stream<BiElement<E1, E2>> usingPrimaryKey(Function<E1, PK> primaryKeyMapper1, Function<E2, PK> primaryKeyMapper2);
    }

    public static <E> Joiner<E> join(Collection<E> elements)
    {
        return join(elements.stream());
    }

    public static <E> Joiner<E> join(Stream<E> elements)
    {
        return new Joiner<E>()
        {

            @Override
            public <E2> JoinerWithElements<E, E2> with(Collection<E2> joinElements)
            {
                return this.with(joinElements.stream());
            }

            @Override
            public <E2> JoinerWithElements<E, E2> with(Stream<E2> joinElements)
            {
                return new JoinerWithElements<E, E2>()
                {
                    @Override
                    public InnerJoiner<E, E2> inner()
                    {
                        return new InnerJoiner<E, E2>()
                        {
                            @Override
                            public <PK> Stream<BiElement<E, E2>> usingPrimaryKey(Function<E, PK> primaryKeyMapper1, Function<E2, PK> primaryKeyMapper2)
                            {
                                Map<PK, E> left = elements.collect(CollectorUtils.groupingByUnique(primaryKeyMapper1));
                                Map<PK, E2> right = joinElements.collect(CollectorUtils.groupingByUnique(primaryKeyMapper2));

                                return SetUtils.merge(left.keySet(), right.keySet())
                                               .stream()
                                               .map(key -> BiElement.of(left.get(key), right.get(key)));
                            }
                        };
                    }

                    @Override
                    public Stream<BiElement<E, E2>> cartesian()
                    {
                        List<E2> joinElementList = joinElements.collect(Collectors.toList());
                        return elements.flatMap(element1 -> joinElementList.stream()
                                                                           .map(element2 -> BiElement.of(element1, element2)));
                    }

                };
            }

        };
    }
}
