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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.omnaest.utils.BeanUtils.BeanAnalyzer;
import org.omnaest.utils.BeanUtils.BeanPropertyAccessor;
import org.omnaest.utils.BeanUtils.NestedFlattenedProperty;
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

    @SafeVarargs
    public static <E> List<E> of(E... elements)
    {
        return new ArrayList<>(Arrays.asList(elements));
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

    public static <E> List<E> addToNew(List<E> list, E element)
    {
        int index = list.size();
        return addToNew(list, index, element);
    }

    public static <E> List<E> addToNew(List<E> list, int index, E element)
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
        while (list.size() < size)
        {
            list.add(elementSupplier.get());
        }
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

}
