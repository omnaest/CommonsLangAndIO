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

import java.util.List;
import java.util.function.Function;

import org.omnaest.utils.ListUtils;
import org.omnaest.utils.list.crud.ReadList;

public interface ListProjectionBuilder
{
	public static interface ElementsSource<E1, E2>
	{
		public E1 getFirstElement();

		public E2 getSecondElement();
	}

	public static interface UnaryElementsSource<E> extends ReadList<E>
	{
		@Override
		public E get(int index);

		@Override
		public int size();

		public static <E> UnaryElementsSource<E> valueOf(List<E> list)
		{
			return new ListToUnaryElementsSourceAdapter<>(list);
		}

		public default List<E> toList()
		{
			return ListUtils.toList(this);
		}
	}

	public static interface UnaryProjectionBuilder
	{
		public <T, E> Readonly<T, E> withReadProjection(Function<UnaryElementsSource<E>, T> readProjection);

		public <T, E> Writeonly<T, E> withWriteProjection(Function<T, UnaryElementsSource<E>> writeProjection);

		public static interface Base<T, E>
		{
			public Sourced<T> usingSources(List<List<E>> lists);
		}

		public static interface Readonly<T, E> extends Base<T, E>
		{
			public ReadAndWrite<T, E> andWriteProjection(Function<T, UnaryElementsSource<E>> writeProjection);
		}

		public static interface Writeonly<T, E> extends Base<T, E>
		{
			public ReadAndWrite<T, E> andReadProjection(Function<UnaryElementsSource<E>, T> readProjection);
		}

		public static interface ReadAndWrite<T, E> extends Base<T, E>
		{
		}

		public static interface Sourced<T>
		{
			public List<T> build();
		}
	}

	public static interface QualifiedProjectionBuilder
	{
		public <T, E1, E2> Readonly<T, E1, E2> withReadProjection(Function<ElementsSource<E1, E2>, T> readProjection);

		public <T, E1, E2> Writeonly<T, E1, E2> withWriteProjection(Function<T, ElementsSource<E1, E2>> writeProjection);

		public static interface Base<T, E1, E2>
		{
			public Sourced<T> usingSources(List<E1> list1, List<E2> list2);

			public Sourced<T> usingSources(E1[] array1, E2[] array2);
		}

		public static interface Readonly<T, E1, E2> extends Base<T, E1, E2>
		{
			public ReadAndWrite<T, E1, E2> andWriteProjection(Function<T, ElementsSource<E1, E2>> writeProjection);
		}

		public static interface Writeonly<T, E1, E2> extends Base<T, E1, E2>
		{
			public ReadAndWrite<T, E1, E2> andReadProjection(Function<ElementsSource<E1, E2>, T> readProjection);
		}

		public static interface ReadAndWrite<T, E1, E2> extends Base<T, E1, E2>
		{
		}

		public static interface Sourced<T>
		{
			public List<T> build();
		}
	}

	public UnaryProjectionBuilder withUnarySource();

	public QualifiedProjectionBuilder withQualifiedSource();
}
