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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.omnaest.utils.BeanUtils.BeanAnalyzer;
import org.omnaest.utils.BeanUtils.BeanPropertyAccessor;
import org.omnaest.utils.list.CRUDList;
import org.omnaest.utils.list.CRUDListToListAdapter;
import org.omnaest.utils.list.ComparableList;
import org.omnaest.utils.list.ComparableListDecorator;

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
		Arrays	.asList(lists)
				.forEach(list -> retlist.addAll(list));
		return retlist;
	}

	public static <E> List<E> shuffled(Collection<E> collection)
	{
		List<E> retlist = new ArrayList<>();
		if (collection != null)
		{
			collection.addAll(collection);
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
				return ComparatorUtils	.<List<E>>chainedComparator(IntStream	.range(0, Math.max(o1.size(), o2.size()))
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

	@SafeVarargs
	public static <E> List<E> of(E... elements)
	{
		return new ArrayList<>(Arrays.asList(elements));
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

	public static <E> boolean isNotEmpty(List<E> list)
	{
		return list != null && !list.isEmpty();
	}

	public static <E> List<E> toList(CRUDList<E> crudList)
	{
		return new CRUDListToListAdapter<>(crudList);
	}

	public static interface ElementsSource<E1, E2>
	{
		public E1 getFirstElement();

		public E2 getSecondElement();
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
	 * Similar to {@link #projection(Function, Function, List, List)} but does throw {@link UnsupportedOperationException}s for any create, update operation
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
		return projection(readProjection, writeProjection, list1, list2);
	}

	/**
	 * Creates a projection from the merged elements of two {@link List} instances into a single {@link List}
	 * 
	 * @param readProjection
	 * @param writeProjection
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static <T, E1, E2> List<T> projection(	Function<ElementsSource<E1, E2>, T> readProjection, Function<T, ElementsSource<E1, E2>> writeProjection,
													List<E1> list1, List<E2> list2)
	{
		CRUDList<T> crudList = new CRUDList<T>()
		{
			@Override
			public T get(int index)
			{
				return readProjection.apply(new ElementsSource<E1, E2>()
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
				ElementsSource<E1, E2> elementsSource = writeProjection.apply(element);
				E1 element1 = list1.set(index, elementsSource.getFirstElement());
				E2 element2 = list2.set(index, elementsSource.getSecondElement());
				return this.wrapSourceElements(readProjection, element1, element2);
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
				ElementsSource<E1, E2> elementsSource = writeProjection.apply(element);
				list1.add(index, elementsSource.getFirstElement());
				list2.add(index, elementsSource.getSecondElement());
			}

			@Override
			public T remove(int index)
			{
				E1 element1 = list1.remove(index);
				E2 element2 = list2.remove(index);
				return this.wrapSourceElements(readProjection, element1, element2);
			}

			@Override
			public int size()
			{
				return Math.max(list1.size(), list2.size());
			}

		};
		return toList(crudList);
	}

	public static interface UnaryElementsSources<E>
	{
		public E get(int index);
	}

	/**
	 * Similar to {@link #projection(Function, Function, List)} but throws {@link UnsupportedOperationException} for add and set access
	 * 
	 * @param readProjection
	 * @param lists
	 * @return
	 */
	public static <T, E> List<T> projection(Function<UnaryElementsSources<E>, T> readProjection, List<List<E>> lists)
	{
		Function<T, UnaryElementsSources<E>> writeProjection = element ->
		{
			throw new UnsupportedOperationException();
		};
		return projection(readProjection, writeProjection, lists);
	}

	public static <T, E> List<T> projection(Function<UnaryElementsSources<E>, T> readProjection, Function<T, UnaryElementsSources<E>> writeProjection,
											List<List<E>> lists)
	{
		CRUDList<T> crudList = new CRUDList<T>()
		{
			@Override
			public T get(int index)
			{
				return readProjection.apply(listIndex -> lists	.get(listIndex)
																.get(index));
			}

			@Override
			public T set(int index, T element)
			{
				UnaryElementsSources<E> elementsSource = writeProjection.apply(element);
				List<E> removedElements = IntStream	.range(0, lists.size())
													.mapToObj(ii -> lists	.get(ii)
																			.set(index, elementsSource.get(ii)))
													.collect(Collectors.toList());
				return this.wrapSourceElements(readProjection, removedElements);
			}

			private T wrapSourceElements(Function<UnaryElementsSources<E>, T> readProjection, List<E> elements)
			{
				return readProjection.apply(new UnaryElementsSources<E>()
				{
					@Override
					public E get(int index)
					{
						return elements.get(index);
					}

				});
			}

			@Override
			public void add(int index, T element)
			{
				UnaryElementsSources<E> elementsSource = writeProjection.apply(element);
				IntStream	.range(0, lists.size())
							.forEach(ii -> lists.get(ii)
												.add(index, elementsSource.get(ii)));
			}

			@Override
			public T remove(int index)
			{
				List<E> removedElements = IntStream	.range(0, lists.size())
													.mapToObj(ii -> lists	.get(ii)
																			.remove(index))
													.collect(Collectors.toList());
				return this.wrapSourceElements(readProjection, removedElements);
			}

			@Override
			public int size()
			{
				return IntStream.range(0, lists.size())
								.map(index -> lists	.get(index)
													.size())
								.max()
								.getAsInt();
			}

		};
		return toList(crudList);
	}

	public static <E> List<E> toMemoryOptimizedList(Class<E> type, List<E> list)
	{
		BeanAnalyzer<E> beanAnalyzer = BeanUtils.analyze(type);
		Map<String, List<Object>> propertyToRawSourceList = new LinkedHashMap<>();
		list.forEach(element ->
		{
			Map<String, Object> propertyMap = beanAnalyzer	.accessAsFlattenedMap(element)
															.asPropertyMap();
			for (String key : propertyMap.keySet())
			{
				propertyToRawSourceList	.computeIfAbsent(key, k -> new ArrayList<>())
										.add(propertyMap.get(key));
			}
		});

		List<List<Object>> rawSourceLists = propertyToRawSourceList	.values()
																	.stream()
																	.collect(Collectors.toList());
		AtomicInteger position = new AtomicInteger();
		Map<String, Integer> propertyToIndex = propertyToRawSourceList	.keySet()
																		.stream()
																		.collect(Collectors.toMap(	property -> property,
																									property -> position.getAndIncrement()));
		Function<UnaryElementsSources<Object>, E> readProjection = sources ->
		{
			Map<String, BeanPropertyAccessor<Object>> propertyToAccessor = propertyToRawSourceList	.keySet()
																									.stream()
																									.collect(Collectors.toMap(	property -> property,
																																property -> (BeanPropertyAccessor<Object>) new BeanPropertyAccessor<Object>()
																																{
																																	@Override
																																	public void accept(Object t)
																																	{
																																		throw new UnsupportedOperationException();
																																	}

																																	@Override
																																	public Object get()
																																	{
																																		Integer index = propertyToIndex.get(property);
																																		return sources.get(index);
																																	}
																																}));
			return beanAnalyzer.newProxy(propertyToAccessor);
		};
		return projection(readProjection, rawSourceLists);
	}

	public static <E> List<E> addToNew(List<E> list, E element)
	{
		int index = list.size();
		return addToNew(list, index, element);
	}

	public static <E> List<E> addToNew(List<E> list, int index, E element)
	{
		List<E> retlist = new ArrayList<>(list);
		retlist.add(index, element);
		return retlist;
	}

}
