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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;
import org.omnaest.utils.BeanUtils.BeanAnalyzer;
import org.omnaest.utils.BeanUtils.BeanPropertyAccessor;
import org.omnaest.utils.BeanUtils.NestedFlattenedProperty;
import org.omnaest.utils.element.bi.UnaryBiElement;
import org.omnaest.utils.element.tri.TriElement;
import org.omnaest.utils.list.ComparableList;
import org.omnaest.utils.list.ComparableListDecorator;
import org.omnaest.utils.list.crud.CRUDList;
import org.omnaest.utils.list.crud.CRUDListToListAdapter;
import org.omnaest.utils.list.crud.ReadList;
import org.omnaest.utils.list.projection.DefaultListAggregationBuilder;
import org.omnaest.utils.list.projection.ListAggregationBuilder;
import org.omnaest.utils.list.projection.ListAggregationBuilder.QualifiedAggregationBuilder;
import org.omnaest.utils.list.projection.ListAggregationBuilder.UnaryElementsSource;

public class ListUtils
{
    public static <E> E last(List<E> list)
    {
        E retval = null;

        if (list != null && !list.isEmpty())
        {
            retval = list.get(list.size() - 1);
        }

        return retval;
    }

    /**
     * Returns the last number of elements of the given {@link List}
     * 
     * @param numberOfElements
     * @param list
     * @return always a new {@link List} but never null
     */
    public static <E> List<E> last(int numberOfElements, List<E> list)
    {
        List<E> result = new ArrayList<>();

        if (list != null && !list.isEmpty())
        {
            int numberOfEffectiveElements = Math.min(numberOfElements, list.size());
            if (numberOfEffectiveElements > 0)
            {
                result.addAll(list.subList(list.size() - numberOfEffectiveElements, list.size()));
            }
        }

        return result;
    }

    public static <E> E first(List<E> list)
    {
        E retval = null;

        if (list != null && !list.isEmpty())
        {
            retval = list.get(0);
        }

        return retval;
    }

    public static <E> Optional<E> optionalFirst(List<E> list)
    {
        return Optional.ofNullable(first(list));
    }

    @SafeVarargs
    public static <E> List<E> mergedList(Collection<E>... lists)
    {
        List<E> retlist = new ArrayList<>();
        Arrays.asList(lists)
              .forEach(list -> retlist.addAll(list));
        return retlist;
    }

    public static <E> List<E> shuffled(Collection<E> collection)
    {
        List<E> retlist = new ArrayList<>();
        if (collection != null)
        {
            retlist.addAll(collection);
        }
        Collections.shuffle(retlist);
        return retlist;
    }

    public static <E> Comparator<List<E>> comparator(Comparator<E> comparator)
    {
        return new Comparator<List<E>>()
        {
            @Override
            public int compare(List<E> o1, List<E> o2)
            {
                return ComparatorUtils.<List<E>>chainedComparator(IntStream.range(0, Math.max(o1.size(), o2.size()))
                                                                           .mapToObj(index -> new Comparator<List<E>>()
                                                                           {
                                                                               @Override
                                                                               public int compare(List<E> o1, List<E> o2)
                                                                               {
                                                                                   E left = index < o1.size() ? o1.get(index) : null;
                                                                                   E right = index < o2.size() ? o2.get(index) : null;
                                                                                   return comparator.compare(left, right);
                                                                               }
                                                                           }))
                                      .compare(o1, o2);

            }
        };
    }

    public static <E> int compare(List<E> o1, List<E> o2, Comparator<E> comparator)
    {
        return comparator(comparator).compare(o1, o2);
    }

    public static <E extends Comparable<E>> ComparableList<E> comparable(List<E> list)
    {
        return new ComparableListDecorator<>(list, ComparatorUtils::compare);
    }

    public static <E> ComparableList<E> comparable(List<E> list, Comparator<E> comparator)
    {
        return new ComparableListDecorator<>(list, comparator);
    }

    public static <E> List<E> defaultIfNull(List<E> list)
    {
        return list != null ? list : Collections.emptyList();
    }

    /**
     * Returns a new {@link List} with the elements of the given {@link Collection} in reverse order.<br>
     * <br>
     * Return null if the given {@link Collection} is null.
     * 
     * @param collection
     * @return
     */
    public static <E> List<E> inverse(Collection<E> collection)
    {
        List<E> retlist = null;
        if (collection != null)
        {
            retlist = new ArrayList<>(collection);
            Collections.reverse(retlist);
        }
        return retlist;
    }

    public static <E> List<E> reverse(Collection<E> collection)
    {
        return inverse(collection);
    }

    @SafeVarargs
    public static <E> List<E> of(E... elements)
    {
        return new ArrayList<>(Arrays.asList(elements));
    }

    /**
     * Returns a new {@link List} instance based on the given {@link Stream}.
     * 
     * @param elements
     * @return
     */
    public static <E> List<E> of(Stream<E> elements)
    {
        return Optional.ofNullable(elements)
                       .map(e -> e.collect(Collectors.toList()))
                       .orElse(Collections.emptyList());
    }

    /**
     * Adds a given element to the given {@link List} or returns a new {@link List} instance if the given {@link List} is null
     * 
     * @param list
     * @param element
     * @return given list instance
     */
    public static <E> List<E> addTo(List<E> list, E element)
    {
        if (list == null)
        {
            list = new ArrayList<>();
        }
        list.add(element);
        return list;
    }

    public static <E> List<E> addTo(List<E> list, int index, E element)
    {
        if (list == null)
        {
            list = new ArrayList<>();
        }
        while (list.size() < index)
        {
            list.add(null);
        }
        list.add(index, element);
        return list;
    }

    public static <E> List<E> addToNew(Collection<E> list, E element)
    {
        int index = list.size();
        return addToNew(list, index, element);
    }

    public static <E> List<E> addToNew(Collection<E> list, int index, E element)
    {
        List<E> retlist = new ArrayList<>(list);
        while (retlist.size() < index)
        {
            retlist.add(null);
        }
        retlist.add(index, element);
        return retlist;
    }

    public static <E> List<E> setTo(List<E> list, int index, E element)
    {
        if (index >= 0)
        {
            ensureSize(list, index + 1);

            list.set(index, element);
        }
        return list;
    }

    /**
     * Fills a {@link List} up to the given size with null entries
     * 
     * @param list
     * @param size
     * @return
     */
    public static <E> List<E> ensureSize(List<E> list, int size)
    {
        return ensureSize(list, size, () -> null);
    }

    /**
     * Ensures the size of a given {@link List} using the given {@link Supplier} to generate elements to fill up the {@link List} if necessary
     * 
     * @param list
     * @param size
     * @param elementSupplier
     * @return
     */
    public static <E> List<E> ensureSize(List<E> list, int size, Supplier<E> elementSupplier)
    {
        return ensureSize(list, size, index -> elementSupplier.get());
    }

    /**
     * Similar to {@link #ensureSize(List, int, Supplier)} but providing an {@link IntFunction} that gets the element index supplied
     * 
     * @param list
     * @param size
     * @param elementFactory
     * @return
     */
    public static <E> List<E> ensureSize(List<E> list, int size, IntFunction<E> elementFactory)
    {
        IntStream.range(list.size(), size)
                 .mapToObj(elementFactory)
                 .forEach(element -> list.add(element));
        return list;
    }

    public static <E> List<E> setTo(List<E> list, int index, Stream<E> elements)
    {
        AtomicInteger position = new AtomicInteger(index);
        if (elements != null)
        {
            elements.forEach(element ->
            {
                setTo(list, position.getAndIncrement(), element);
            });
        }
        return list;
    }

    @SafeVarargs
    public static <E> List<E> modified(List<E> list, UnaryOperator<List<E>>... modifiers)
    {
        List<E> retlist = list;
        if (modifiers != null)
        {
            for (UnaryOperator<List<E>> modifier : modifiers)
            {
                retlist = modifier.apply(retlist);
            }
        }
        return retlist;
    }

    /**
     * Returns true if the given {@link List} is not null and not {@link List#isEmpty()}
     * 
     * @param list
     * @return
     */
    public static <E> boolean isNotEmpty(List<E> list)
    {
        return list != null && !list.isEmpty();
    }

    /**
     * @see CRUDList
     * @param crudList
     * @return
     */
    public static <E> List<E> toList(CRUDList<E> crudList)
    {
        return new CRUDListToListAdapter<>(crudList);
    }

    /**
     * @see ReadList
     * @param readList
     * @return
     */
    public static <E> List<E> toList(ReadList<E> readList)
    {
        return toList(CRUDList.valueOf(readList));
    }

    public static <E> CRUDList<E> toCRUDList(List<E> list)
    {
        return CRUDList.valueOf(list);
    }

    /**
     * @see QualifiedAggregationBuilder
     * @return
     */
    public static ListAggregationBuilder aggregation()
    {
        return new DefaultListAggregationBuilder();
    }

    private static class BreakFunctionImpl<E> implements Function<E, List<Object>>
    {
        @Override
        public List<Object> apply(E element)
        {
            List<Object> retlist;
            boolean propertiesChanged = false;
            do
            {
                retlist = new ArrayList<>();
                for (NestedFlattenedProperty flattenedProperty : this.properties.values()
                                                                                .stream()
                                                                                .collect(Collectors.toList()))
                {
                    Object retval = flattenedProperty.accessFromRoot(Object.class, element)
                                                     .get();
                    if (retval != null)
                    {
                        propertiesChanged = this.propertyExplodingConsumer.apply(flattenedProperty);
                        if (propertiesChanged)
                        {
                            break;
                        }
                    }

                    retlist.add(retval);
                }
            } while (propertiesChanged);
            return retlist;
        }

        private BeanAnalyzer<E> beanAnalyzer;

        private Map<String, NestedFlattenedProperty> properties      = new LinkedHashMap<>();
        private AtomicInteger                        position        = new AtomicInteger();
        private Map<String, Integer>                 propertyToIndex = new LinkedHashMap<>();

        private Consumer<Stream<NestedFlattenedProperty>> propertiesBuilder = newProperties ->
        {
            Map<String, NestedFlattenedProperty> collectedProperties = newProperties.collect(Collectors.toMap(property -> property.getPropertyName(),
                                                                                                              property -> property));
            this.properties.putAll(collectedProperties);
            this.propertyToIndex.putAll(collectedProperties.keySet()
                                                           .stream()
                                                           .collect(Collectors.toMap(property -> property, property -> this.position.getAndIncrement())));
        };

        private Set<String>                                alreadyExplodedProperties = new HashSet<>();
        private Function<NestedFlattenedProperty, Boolean> propertyExplodingConsumer = nestedProperty ->
                                                                                     {
                                                                                         boolean retval = false;
                                                                                         String propertyName = nestedProperty.getPropertyName();
                                                                                         if (!this.alreadyExplodedProperties.contains(propertyName)
                                                                                                 && !nestedProperty.getProperty()
                                                                                                                   .hasPrimitiveType())
                                                                                         {
                                                                                             this.alreadyExplodedProperties.add(propertyName);
                                                                                             this.propertiesBuilder.accept(nestedProperty.getSubProperties());
                                                                                             retval = true;
                                                                                         }
                                                                                         return retval;
                                                                                     };

        public BreakFunctionImpl(Class<E> type)
        {
            super();
            this.beanAnalyzer = BeanUtils.analyze(type);

            //root properties
            this.propertiesBuilder.accept(this.beanAnalyzer.getNestedFlattenedProperties());
        }

        public MergeFunctionImpl<E> spawnMergeFunction()
        {
            return new MergeFunctionImpl<>(this.beanAnalyzer, this.propertyToIndex);
        }
    }

    private static class MergeFunctionImpl<E> implements Function<List<Object>, E>
    {
        private BeanAnalyzer<E>      beanAnalyzer;
        private Map<String, Integer> propertyToIndex;

        public MergeFunctionImpl(BeanAnalyzer<E> beanAnalyzer, Map<String, Integer> propertyToIndex)
        {
            super();
            this.beanAnalyzer = beanAnalyzer;
            this.propertyToIndex = propertyToIndex;
        }

        @Override
        public E apply(List<Object> elements)
        {
            return this.beanAnalyzer.newProxy(this.propertyToIndex.keySet()
                                                                  .stream()
                                                                  .collect(Collectors.toMap(property1 -> property1,
                                                                                            property2 -> (BeanPropertyAccessor<Object>) new BeanPropertyAccessor<Object>()
                                                                                            {
                                                                                                @Override
                                                                                                public void accept(Object t)
                                                                                                {
                                                                                                    throw new UnsupportedOperationException();
                                                                                                }

                                                                                                @Override
                                                                                                public Object get()
                                                                                                {
                                                                                                    Integer index = MergeFunctionImpl.this.propertyToIndex.get(property2);
                                                                                                    return elements.get(index);
                                                                                                }
                                                                                            })));
        }
    }

    public static <E> List<E> toMemoryOptimizedList(Class<E> type, List<E> list)
    {
        //
        BreakFunctionImpl<E> breakFunction = new BreakFunctionImpl<E>(type);
        Function<List<Object>, E> mergeFunction = breakFunction.spawnMergeFunction();
        return toMemoryOptimizedList(type, list, breakFunction, mergeFunction);
    }

    public static <E> List<E> toMemoryOptimizedList(Class<E> type, List<E> list, Function<E, List<Object>> breakFunction,
                                                    Function<List<Object>, E> mergeFunction)
    {
        List<List<Object>> rawSourceLists = new ArrayList<>();
        list.stream()
            .map(element -> breakFunction.apply(element))
            .forEach(elements ->
            {
                while (rawSourceLists.size() < elements.size())
                {
                    rawSourceLists.add(new ArrayList<>());
                }
                int insertPosition = rawSourceLists.get(0)
                                                   .size();
                for (int ii = 0; ii < elements.size(); ii++)
                {
                    ListUtils.addTo(rawSourceLists.get(ii), insertPosition, elements.get(ii));
                }
            });
        List<List<Object>> finalizedRawSourceLists = rawSourceLists;
        rawSourceLists.stream()
                      .map(ilist -> Arrays.asList(ilist.toArray()))
                      .collect(Collectors.toList());
        Function<UnaryElementsSource<Object>, E> readProjection = sources -> mergeFunction.apply(sources.toList());
        return aggregation().withUnarySource()
                            .withReadProjection(readProjection)
                            .usingSources(finalizedRawSourceLists)
                            .build();
    }

    /**
     * Returns the element at the given index position of the given {@link List} but never throws an {@link IndexOutOfBoundsException}s instead returns null.
     * 
     * @param list
     * @param index
     * @return
     */
    public static <E> E get(List<E> list, int index)
    {
        E retval = null;

        if (list != null && index >= 0 && index < list.size())
        {
            retval = list.get(index);
        }

        return retval;

    }

    /**
     * Returns an array of the given element {@link Class} type containing the elements of the given {@link List}
     * 
     * @param list
     * @param elementType
     * @return
     */
    public static <E> E[] toArray(List<E> list, Class<E> elementType)
    {
        @SuppressWarnings("unchecked")
        E[] array = (E[]) Array.newInstance(elementType, list.size());
        return list.toArray(array);
    }

    /**
     * Returns a random element from the given {@link List}
     * 
     * @param list
     * @return
     */
    public static <E> Optional<E> getRandomElement(List<E> list)
    {
        return list == null || list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(RandomUtils.nextInt(0, list.size())));
    }

    /**
     * Returns a {@link Stream} of random elements from the {@link Collection}. All elements available in the {@link Collection} will be only present once in
     * the returned {@link Stream}.
     * 
     * @param collection
     * @return
     */
    public static <E> Stream<E> getRandomElementStream(Collection<E> collection)
    {
        ArrayList<E> randomList = new ArrayList<>(collection);
        Collections.shuffle(randomList);
        return randomList.stream();
    }

    public static <E> List<E> toList(Iterable<E> iterable)
    {
        return iterable != null ? toList(iterable.iterator()) : new ArrayList<>();
    }

    public static <E> List<E> toList(Iterator<E> iterator)
    {
        List<E> result = new ArrayList<>();

        if (iterator != null)
        {
            while (iterator.hasNext())
            {
                result.add(iterator.next());
            }
        }

        return result;
    }

    @SafeVarargs
    public static <E> List<E> toList(E... elements)
    {
        List<E> result = new ArrayList<>();

        if (elements != null)
        {
            result.addAll(Arrays.asList(elements));
        }

        return result;
    }

    public static <E> Set<Set<E>> allCombinations(List<E> list)
    {
        Set<Set<E>> result = new HashSet<>();

        if (list != null && !list.isEmpty())
        {
            IntStream.range(0, list.size())
                     .forEach(index ->
                     {
                         TriElement<E, List<E>, List<E>> splits = ListUtils.removeAtAndSplit(list, index);

                         E element = splits.getFirst();
                         List<E> mergedRest = ListUtils.mergedList(splits.getSecond(), splits.getThird());

                         result.add(Arrays.asList(element)
                                          .stream()
                                          .collect(Collectors.toSet()));

                         result.addAll(allCombinations(mergedRest).stream()
                                                                  .map(set -> SetUtils.addToNew(set, element))
                                                                  .collect(Collectors.toList()));
                     });
        }

        return result;
    }

    public static <E> TriElement<E, List<E>, List<E>> removeAtAndSplit(Collection<E> collection, int index)
    {
        List<E> list = collection == null ? null
                : collection.stream()
                            .collect(Collectors.toList());
        if (list != null && index >= 0 && index < list.size())
        {
            return TriElement.of(list.get(index), list.subList(0, index), list.subList(index + 1, list.size()));
        }
        else if (list != null)
        {
            return TriElement.of(null, list, Collections.emptyList());
        }
        else
        {
            return TriElement.empty();
        }
    }

    public static <E> UnaryBiElement<List<E>> split(int splitSize, List<E> list)
    {
        return UnaryBiElement.of(list.subList(0, splitSize), list.subList(splitSize, list.size()));
    }

    public static <E> Set<List<E>> allPermutations(Collection<E> collection)
    {
        Set<List<E>> result = new HashSet<>();

        IntStream.range(0, collection.size())
                 .forEach(index ->
                 {
                     TriElement<E, List<E>, List<E>> splits = ListUtils.removeAtAndSplit(collection, index);

                     E element = splits.getFirst();
                     List<E> rest = ListUtils.mergedList(splits.getSecond(), splits.getThird());

                     if (!rest.isEmpty())
                     {
                         allPermutations(rest).forEach(iElement -> result.add(ListUtils.addToNew(iElement, element)));
                     }
                     else
                     {
                         result.add(Arrays.asList(element));
                     }
                 });

        return result;
    }

    public static <E> boolean equals(List<E> list1, List<E> list2)
    {
        return list1 != null && list2 != null && new ArrayList<>(list1).equals(new ArrayList<>(list2));
    }

    public static <E> int hashCode(List<E> list)
    {
        return new ArrayList<>(list).hashCode();
    }

    public static <E> String toString(List<E> list)
    {
        return "[" + (list != null ? list.stream() : Stream.empty()).map(element -> StringUtils.toString(element))
                                                                    .collect(Collectors.joining(","))
                + "]";
    }

    public static <E> Stream<E> toStream(List<E> list)
    {
        return list != null ? list.stream() : Stream.empty();
    }

    /**
     * Returns all possible sublists from start to the current pointer position. And the pointer position moves from the end to the start.
     * <br>
     * <br>
     * Example: [0,1,2] -> [[0,1,2],[0,1],[0]]
     * 
     * @param list
     * @return
     */
    public static <E> Stream<List<E>> sublistsFromStart(List<E> list)
    {
        return StreamUtils.generate()
                          .intStream()
                          .limited()
                          .withMaxExclusive(list.size())
                          .fromZero()
                          .map(index -> list.size() - index)
                          .mapToObj(index -> list.subList(0, index));
    }

    /**
     * Returns a new {@link List} instance beginning at the given start position of the given {@link List}.<br>
     * <br>
     * If null is given an empty {@link List} is returned.
     * 
     * @param list
     * @param startInclusive
     * @return
     */
    public static <E> List<E> sublist(List<E> list, int startInclusive)
    {
        return sublist(list, startInclusive, Optional.ofNullable(list)
                                                     .map(List<E>::size)
                                                     .orElse(0));
    }

    /**
     * Returns a new {@link List} instance beginning at the given start and ending of the given {@link List}.<br>
     * <br>
     * The end is exclusive.
     * <br>
     * <br>
     * If null is given as {@link List} and empty {@link List} is returned.
     * 
     * @param list
     * @param startInclusive
     * @param endExclusive
     * @return
     */
    public static <E> List<E> sublist(List<E> list, int startInclusive, int endExclusive)
    {
        return Optional.ofNullable(list)
                       .orElse(Collections.emptyList())
                       .stream()
                       .skip(startInclusive)
                       .limit(Math.max(0, endExclusive - startInclusive))
                       .collect(Collectors.toList());
    }

    /**
     * Returns a new {@link List} instance beginning at the start and ending a the given distance to the size of the given {@link List}.<br>
     * <br>
     * If null is given an empty {@link List} is returned.
     * 
     * @param list
     * @param fromEnd
     * @return
     */
    public static <E> List<E> sublistFromEnd(List<E> list, int fromEnd)
    {
        return sublist(list, 0, Optional.ofNullable(list)
                                        .map(List<E>::size)
                                        .map(size -> size - fromEnd)
                                        .orElse(0));
    }

}
