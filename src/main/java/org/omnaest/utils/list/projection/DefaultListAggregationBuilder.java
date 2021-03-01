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
package org.omnaest.utils.list.projection;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.omnaest.utils.ListUtils;
import org.omnaest.utils.list.crud.CRUDList;

public class DefaultListAggregationBuilder implements ListAggregationBuilder
{

    private static class UnaryAggregationBuilderImpl<T, E> implements UnaryAggregationBuilder, UnaryAggregationBuilder.Sourced<T>,
            UnaryAggregationBuilder.Readonly<T, E>, UnaryAggregationBuilder.Writeonly<T, E>, UnaryAggregationBuilder.ReadAndWrite<T, E>
    {
        private Function<UnaryElementsSource<E>, T> readAggregation;
        private Function<T, UnaryElementsSource<E>> writeAggregation;
        private List<List<E>>                       lists;

        public UnaryAggregationBuilderImpl(Function<UnaryElementsSource<E>, T> readProjection, Function<T, UnaryElementsSource<E>> writeProjection)
        {
            super();
            this.readAggregation = readProjection;
            this.writeAggregation = writeProjection;
        }

        public UnaryAggregationBuilderImpl()
        {
            super();
        }

        @Override
        public <TN, EN> Readonly<TN, EN> withReadProjection(Function<UnaryElementsSource<EN>, TN> readProjection)
        {
            return new UnaryAggregationBuilderImpl<>(readProjection, null);
        }

        @Override
        public <TN, EN> Writeonly<TN, EN> withWriteProjection(Function<TN, UnaryElementsSource<EN>> writeProjection)
        {
            return new UnaryAggregationBuilderImpl<>(null, writeProjection);
        }

        @Override
        public Sourced<T> usingSources(List<List<E>> lists)
        {
            this.lists = lists;
            return this;
        }

        @Override
        public ReadAndWrite<T, E> andReadProjection(Function<UnaryElementsSource<E>, T> readProjection)
        {
            this.readAggregation = readProjection;
            return this;
        }

        @Override
        public ReadAndWrite<T, E> andWriteProjection(Function<T, UnaryElementsSource<E>> writeProjection)
        {
            this.writeAggregation = writeProjection;
            return this;
        }

        @Override
        public List<T> build()
        {
            return aggregation(this.readAggregation, this.writeAggregation, this.lists);
        }

    }

    private static class QualifiedAggregationBuilderImpl<T, E1, E2>
            implements QualifiedAggregationBuilder, QualifiedAggregationBuilder.Sourced<T>, QualifiedAggregationBuilder.ReadAndWrite<T, E1, E2>,
            QualifiedAggregationBuilder.Readonly<T, E1, E2>, QualifiedAggregationBuilder.Writeonly<T, E1, E2>
    {
        private Function<ElementsSource<E1, E2>, T> readProjection;
        private Function<T, ElementsSource<E1, E2>> writeProjection;
        private List<E1>                            list1;
        private List<E2>                            list2;

        public QualifiedAggregationBuilderImpl(Function<ElementsSource<E1, E2>, T> readProjection, Function<T, ElementsSource<E1, E2>> writeProjection)
        {
            super();
            this.readProjection = readProjection;
            this.writeProjection = writeProjection;
        }

        public QualifiedAggregationBuilderImpl()
        {
            super();
        }

        @Override
        public QualifiedAggregationBuilderImpl<T, E1, E2> andReadAggregation(Function<ElementsSource<E1, E2>, T> readProjection)
        {
            this.readProjection = readProjection;
            return this;
        }

        @Override
        public QualifiedAggregationBuilderImpl<T, E1, E2> andWriteAggregation(Function<T, ElementsSource<E1, E2>> writeProjection)
        {
            this.writeProjection = writeProjection;
            return this;
        }

        @Override
        public <TN, EN1, EN2> QualifiedAggregationBuilderImpl<TN, EN1, EN2> withReadAggregation(Function<ElementsSource<EN1, EN2>, TN> readProjection)
        {
            return new QualifiedAggregationBuilderImpl<>(readProjection, null);
        }

        @Override
        public <TN, EN1, EN2> QualifiedAggregationBuilderImpl<TN, EN1, EN2> withWriteAggregation(Function<TN, ElementsSource<EN1, EN2>> writeProjection)
        {
            return new QualifiedAggregationBuilderImpl<>(null, writeProjection);
        }

        @Override
        public QualifiedAggregationBuilderImpl<T, ?, ?> usingSources(List<E1> list1, List<E2> list2)
        {
            this.list1 = list1;
            this.list2 = list2;
            return this;
        }

        @Override
        public Sourced<T> usingSources(E1[] array1, E2[] array2)
        {
            return this.usingSources(Arrays.asList(array1), Arrays.asList(array2));
        }

        @Override
        public List<T> build()
        {
            return aggregation(this.readProjection, this.writeProjection, this.list1, this.list2);
        }

    }

    @Override
    public UnaryAggregationBuilder withUnarySource()
    {
        return new UnaryAggregationBuilderImpl<Object, Object>();
    }

    @Override
    public QualifiedAggregationBuilder withQualifiedSource()
    {
        return new QualifiedAggregationBuilderImpl<Object, Object, Object>();
    }

    /**
     * Creates a projection from the merged elements of two {@link List} instances into a single {@link List}
     * 
     * @param readAggregation
     * @param writeAggregation
     * @param list1
     * @param list2
     * @return
     */
    public static <T, E1, E2> List<T> aggregation(Function<ElementsSource<E1, E2>, T> readAggregation, Function<T, ElementsSource<E1, E2>> writeAggregation,
                                                  List<E1> list1, List<E2> list2)
    {
        CRUDList<T> crudList = new CRUDList<T>()
        {
            @Override
            public T get(int index)
            {
                return readAggregation.apply(new ElementsSource<E1, E2>()
                {
                    @Override
                    public E1 getFirstElement()
                    {
                        return list1.get(index);
                    }

                    @Override
                    public E2 getSecondElement()
                    {
                        return list2.get(index);
                    }
                });
            }

            @Override
            public T set(int index, T element)
            {
                ElementsSource<E1, E2> elementsSource = writeAggregation.apply(element);
                E1 element1 = list1.set(index, elementsSource.getFirstElement());
                E2 element2 = list2.set(index, elementsSource.getSecondElement());
                return this.wrapSourceElements(readAggregation, element1, element2);
            }

            private T wrapSourceElements(Function<ElementsSource<E1, E2>, T> readProjection, E1 element1, E2 element2)
            {
                return readProjection.apply(new ElementsSource<E1, E2>()
                {
                    @Override
                    public E1 getFirstElement()
                    {
                        return element1;
                    }

                    @Override
                    public E2 getSecondElement()
                    {
                        return element2;
                    }
                });
            }

            @Override
            public void add(int index, T element)
            {
                ElementsSource<E1, E2> elementsSource = writeAggregation.apply(element);
                list1.add(index, elementsSource.getFirstElement());
                list2.add(index, elementsSource.getSecondElement());
            }

            @Override
            public T remove(int index)
            {
                E1 element1 = list1.remove(index);
                E2 element2 = list2.remove(index);
                return this.wrapSourceElements(readAggregation, element1, element2);
            }

            @Override
            public int size()
            {
                return Math.max(list1.size(), list2.size());
            }

        };
        return ListUtils.toList(crudList);
    }

    /**
     * Similar to {@link #projection(Function, List, List)} using {@link Arrays#asList(Object...)}
     * 
     * @param readProjection
     * @param array1
     * @param array2
     * @return
     */
    public static <T, E1, E2> List<T> projection(Function<ElementsSource<E1, E2>, T> readProjection, E1[] array1, E2[] array2)
    {
        return projection(readProjection, Arrays.asList(array1), Arrays.asList(array2));
    }

    /**
     * Similar to {@link #aggregation(Function, Function, List, List)} but does throw {@link UnsupportedOperationException}s for any create, update operation
     * 
     * @param readProjection
     * @param list1
     * @param list2
     * @return
     */
    public static <T, E1, E2> List<T> projection(Function<ElementsSource<E1, E2>, T> readProjection, List<E1> list1, List<E2> list2)
    {
        Function<T, ElementsSource<E1, E2>> writeProjection = t ->
        {
            throw new UnsupportedOperationException();
        };
        return aggregation(readProjection, writeProjection, list1, list2);
    }

    /**
     * Similar to {@link #aggregation(Function, Function, List)} but throws {@link UnsupportedOperationException} for add and set access
     * 
     * @param readAggregation
     * @param lists
     * @return
     */
    public static <T, E> List<T> aggregation(Function<UnaryElementsSource<E>, T> readAggregation, List<List<E>> lists)
    {
        Function<T, UnaryElementsSource<E>> writeAggregation = element ->
        {
            throw new UnsupportedOperationException();
        };
        return aggregation(readAggregation, writeAggregation, lists);
    }

    public static <T, E> List<T> aggregation(Function<UnaryElementsSource<E>, T> readAggregation, Function<T, UnaryElementsSource<E>> writeAggregation,
                                             List<List<E>> lists)
    {
        CRUDList<T> crudList = new CRUDList<T>()
        {
            @Override
            public T get(int index)
            {
                return readAggregation.apply(new UnaryElementsSource<E>()
                {
                    @Override
                    public E get(int listIndex)
                    {
                        return lists.get(listIndex)
                                    .get(index);
                    }

                    @Override
                    public int size()
                    {
                        return lists.size();
                    }

                });
            }

            @Override
            public T set(int index, T element)
            {
                UnaryElementsSource<E> elementsSource = writeAggregation.apply(element);
                List<E> removedElements = IntStream.range(0, lists.size())
                                                   .mapToObj(ii -> lists.get(ii)
                                                                        .set(index, elementsSource.get(ii)))
                                                   .collect(Collectors.toList());
                return this.wrapSourceElements(readAggregation, removedElements);
            }

            private T wrapSourceElements(Function<UnaryElementsSource<E>, T> readProjection, List<E> elements)
            {
                return readProjection.apply(new UnaryElementsSource<E>()
                {
                    @Override
                    public E get(int index)
                    {
                        return elements.get(index);
                    }

                    @Override
                    public int size()
                    {
                        return elements.size();
                    }
                });
            }

            @Override
            public void add(int index, T element)
            {
                UnaryElementsSource<E> elementsSource = writeAggregation.apply(element);
                IntStream.range(0, lists.size())
                         .forEach(ii -> lists.get(ii)
                                             .add(index, elementsSource.get(ii)));
            }

            @Override
            public T remove(int index)
            {
                List<E> removedElements = IntStream.range(0, lists.size())
                                                   .mapToObj(ii -> lists.get(ii)
                                                                        .remove(index))
                                                   .collect(Collectors.toList());
                return this.wrapSourceElements(readAggregation, removedElements);
            }

            @Override
            public int size()
            {
                return IntStream.range(0, lists.size())
                                .map(index -> lists.get(index)
                                                   .size())
                                .max()
                                .getAsInt();
            }

        };
        return ListUtils.toList(crudList);
    }
}
