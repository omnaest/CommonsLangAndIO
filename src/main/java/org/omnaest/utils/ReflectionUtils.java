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

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ReflectionUtils
{
	private static class MethodImpl<T> implements Method<T>
	{
		private java.lang.reflect.Method method;

		public MethodImpl(java.lang.reflect.Method method)
		{
			this.method = method;
		}

		@Override
		public java.lang.reflect.Method getRawMethod()
		{
			return this.method;
		}

		@Override
		public String getName()
		{
			return this.method.getName();
		}

		@Override
		public Class<?> getReturnType()
		{
			return this.method.getReturnType();
		}

		@Override
		public List<Class<?>> getParameterTypes()
		{
			return Arrays.asList(this.method.getParameterTypes());
		}

		@Override
		public MethodAccessor access(T instance)
		{
			return new MethodAccessor()
			{
				@SuppressWarnings("unchecked")
				@Override
				public <E> void setValue(E element)
				{
					this.invoke(element);
				}

				@Override
				public <E> E getValue()
				{
					return this.invoke();
				}

				@SuppressWarnings("hiding")
				@Override
				public <T> GetterMethodAccessor<T> asGetter()
				{
					return () -> this.getValue();
				}

				@SuppressWarnings("hiding")
				@Override
				public <T> SetterMethodAccessor<T> asSetter()
				{
					return t -> this.setValue(t);
				}

				@SuppressWarnings("unchecked")
				@Override
				public <E, R> R invoke(E... args)
				{
					R retval = null;
					try
					{
						retval = (R) MethodImpl.this.method.invoke(instance, args);
					} catch (Exception e)
					{
						new IllegalStateException(e);
					}

					return retval;
				}

			};

		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.method == null) ? 0 : this.method.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (this.getClass() != obj.getClass())
			{
				return false;
			}
			@SuppressWarnings("rawtypes")
			MethodImpl other = (MethodImpl) obj;
			if (this.method == null)
			{
				if (other.method != null)
				{
					return false;
				}
			}
			else if (!this.method.equals(other.method))
			{
				return false;
			}
			return true;
		}

		@Override
		public String toString()
		{
			return "MethodImpl [getName()=" + this.getName() + ", getReturnType()=" + this.getReturnType() + ", getParameterTypes()=" + this.getParameterTypes()
					+ "]";
		}

	}

	public static interface Method<T>
	{
		public String getName();

		public MethodAccessor access(T instance);

		public Class<?> getReturnType();

		public List<Class<?>> getParameterTypes();

		public java.lang.reflect.Method getRawMethod();
	}

	public static interface MethodAccessor
	{

		@SuppressWarnings("unchecked")
		public <E, R> R invoke(E... args);

		public <E> void setValue(E element);

		public <E> E getValue();

		public <T> GetterMethodAccessor<T> asGetter();

		public <T> SetterMethodAccessor<T> asSetter();
	}

	public static interface GetterMethodAccessor<T> extends Supplier<T>
	{
	}

	public static interface SetterMethodAccessor<T> extends Consumer<T>
	{
	}

	public static interface TypeReflection<T>
	{
		public Stream<Class<?>> getSuperTypes();

		public Stream<Class<?>> getInterfaces();

		public Stream<Class<?>> getSuperTypesAndInterfaces();

		public Stream<Method<T>> getMethods();
	}

	public static <T> TypeReflection<T> of(Class<T> type)
	{
		return new TypeReflection<T>()
		{
			@Override
			public Stream<Class<?>> getInterfaces()
			{
				return Arrays	.asList(type.getInterfaces())
								.stream()
								.filter(interfaceType -> interfaceType != null)
								.flatMap(parentType -> Stream.concat(Stream.of(parentType), ReflectionUtils	.of(parentType)
																											.getInterfaces()));
			}

			@Override
			public Stream<Class<?>> getSuperTypes()
			{
				return Arrays	.asList(type.getSuperclass())
								.stream()
								.filter(superType -> superType != null)
								.flatMap(parentType -> Stream.concat(Stream.of(parentType), ReflectionUtils	.of(parentType)
																											.getSuperTypes()));
			}

			@Override
			public Stream<Class<?>> getSuperTypesAndInterfaces()
			{
				return Stream.concat(this.getSuperTypes(), this.getInterfaces());
			}

			@Override
			public Stream<Method<T>> getMethods()
			{
				List<java.lang.reflect.Method> declaredMethods = Arrays.asList(type.getDeclaredMethods());
				return Stream	.concat(declaredMethods.stream(), this	.getSuperTypesAndInterfaces()
																		.flatMap(parentType -> Arrays	.asList(parentType.getDeclaredMethods())
																										.stream()))
								.distinct()
								.map(MethodImpl::new);
			}

		};
	}

}
