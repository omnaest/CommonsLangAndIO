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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.omnaest.utils.ProxyUtils.MethodAndHandler;
import org.omnaest.utils.ProxyUtils.MethodHandler;
import org.omnaest.utils.ReflectionUtils.Method;
import org.omnaest.utils.map.MapDecorator;

public class BeanUtils
{
	public static interface PropertyAccessor<E> extends UnknownTypePropertyAccessor
	{
		public E get();

		public void set(E element);

		@Override
		public Class<E> getType();
	}

	public static interface UnknownTypePropertyAccessor
	{
		public <E> PropertyAccessor<E> as(Class<E> type);

		public boolean hasPrimitiveType();

		public Class<?> getType();
	}

	public static interface AccessMethods<T>
	{
		public Method<T> getReadMethod();

		public Method<T> getWriteMethod();
	}

	public static interface Property<T>
	{
		public String getName();

		public AccessMethods<T> getAccessMethods();

		public <E> PropertyAccessor<E> access(Class<E> propertyType, T instance);

		public <E> PropertyAccessor<E> access(Class<E> propertyType, Supplier<T> instance);
	}

	public static interface BeanAnalyzer<T>
	{
		public Stream<Property<T>> getProperties();

		public PropertyAccessorMap accessAsMap(T instance);

		public PropertyAccessorMap accessAsMap(Supplier<T> instance);

		public FlattenedPropertyAccessorMap accessAsFlattenedMap(T instance);

		public FlattenedPropertyAccessorMap accessAsFlattenedMap(Supplier<T> instance);

		public T newProxy(Map<String, ? extends BeanPropertyAccessor<?>> propertyToAccessor);

		public T newProxy(Consumer<BeanPropertyAccessorRegistry> registerStream);

	}

	public static interface BeanPropertyAccessorRegistry
	{
		public <T> void attach(BeanPropertyAccessor<T> accessor);

		public Property<?> getProperty();

		public <T> BeanPropertyAccessorRegistry attachIf(Predicate<Property<T>> condition, BeanPropertyAccessor<T> accessor);
	}

	private static abstract class BeanPropertyAccessorRegistryImpl implements BeanPropertyAccessorRegistry
	{
		private Property<?> property;

		public BeanPropertyAccessorRegistryImpl(Property<?> property)
		{
			this.property = property;
		}

		@Override
		public Property<?> getProperty()
		{
			return this.property;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> BeanPropertyAccessorRegistry attachIf(Predicate<Property<T>> condition, BeanPropertyAccessor<T> accessor)
		{
			if (condition.test((Property<T>) this.getProperty()))
			{
				this.attach(accessor);
			}
			return this;
		}

	}

	public static interface PropertyAccessorMap extends Map<String, UnknownTypePropertyAccessor>
	{

	}

	public static interface FlattenedPropertyAccessorMap extends Map<List<String>, UnknownTypePropertyAccessor>
	{
		/**
		 * Returns a new {@link Map} instance with all properties converted to a property syntax where keys are like "parent.field","field",... and values read
		 * as {@link Object}
		 * 
		 * @return
		 */
		public Map<String, Object> asPropertyMap();

	}

	private static class FlattenedPropertyAccessorMapImpl extends MapDecorator<List<String>, UnknownTypePropertyAccessor>
			implements FlattenedPropertyAccessorMap
	{

		public FlattenedPropertyAccessorMapImpl(Map<List<String>, UnknownTypePropertyAccessor> map)
		{
			super(map);
		}

		@Override
		public Map<String, Object> asPropertyMap()
		{
			return this	.entrySet()
						.stream()
						.collect(Collectors.toMap(	entry -> entry	.getKey()
																	.stream()
																	.collect(Collectors.joining(".")),
													entry ->
													{
														UnknownTypePropertyAccessor propertyAccessor = entry.getValue();
														return propertyAccessor	.as(String.class)
																				.get();
													}));
		}
	}

	private static class PropertyAccessorMapImpl extends MapDecorator<String, UnknownTypePropertyAccessor> implements PropertyAccessorMap
	{

		public PropertyAccessorMapImpl(Map<String, UnknownTypePropertyAccessor> map)
		{
			super(map);
		}

	}

	public static interface BeanPropertyAccessor<T> extends Consumer<T>, Supplier<T>
	{

	}

	public static <T> BeanAnalyzer<T> analyze(Class<T> type)
	{
		return new BeanAnalyzer<T>()
		{
			@Override
			public T newProxy(Consumer<BeanPropertyAccessorRegistry> registerStream)
			{
				Map<Property<?>, BeanPropertyAccessor<?>> propertyToAccessors = new HashMap<>();
				this.getProperties()
					.map(property -> new BeanPropertyAccessorRegistryImpl(property)
					{
						@Override
						public <T1> void attach(BeanPropertyAccessor<T1> accessor)
						{
							propertyToAccessors.put(property, accessor);
						}
					})
					.forEach(registry -> registerStream.accept(registry));
				return ProxyUtils	.builder()
									.of(type)
									.withHandler(propertyToAccessors.entrySet()
																	.stream()
																	.flatMap(entry ->
																	{
																		Property<?> property = entry.getKey();
																		BeanPropertyAccessor<?> beanPropertyAccessor = entry.getValue();

																		AccessMethods<?> accessMethods = property.getAccessMethods();
																		Method<?> readMethod = accessMethods.getReadMethod();
																		Method<?> writeMethod = accessMethods.getWriteMethod();

																		List<MethodAndHandler> methodAndHandlers = new ArrayList<>();
																		if (readMethod != null)
																		{
																			methodAndHandlers.add(new ProxyUtils.MethodAndHandler()
																			{
																				@Override
																				public MethodHandler getMethodHandler()
																				{
																					return arguments -> beanPropertyAccessor.get();
																				}

																				@Override
																				public Method<?> getMethod()
																				{
																					return readMethod;
																				}
																			});
																		}
																		if (writeMethod != null)
																		{
																			methodAndHandlers.add(new ProxyUtils.MethodAndHandler()
																			{
																				@Override
																				public MethodHandler getMethodHandler()
																				{
																					return arguments ->
																					{
																						beanPropertyAccessor.accept(arguments	.first()
																																.get());
																						return null;
																					};
																				}

																				@Override
																				public Method<?> getMethod()
																				{
																					return writeMethod;
																				}
																			});
																		}

																		return methodAndHandlers.stream();
																	}))
									.build();
			}

			@Override
			public T newProxy(Map<String, ? extends BeanPropertyAccessor<?>> accessors)
			{
				return this.newProxy(registry -> registry.attachIf(property -> accessors.containsKey(property.getName()), accessors.get(registry.getProperty()
																																				.getName())));
			}

			@Override
			public Stream<Property<T>> getProperties()
			{
				Map<String, List<Method<T>>> propertyToMethods = ReflectionUtils.of(type)
																				.getMethods()
																				.filter(this::isPropertyMethod)
																				.collect(Collectors.groupingBy(method -> this.determinePropertyName(method.getName())));
				return propertyToMethods.entrySet()
										.stream()
										.map(entry -> this.wrapMethodAsProperty(entry.getKey(), this.determineReadMethod(entry.getValue()),
																				this.determineWriteMethod(entry.getValue())));
			}

			private boolean isPropertyMethod(Method<T> method)
			{
				boolean isGetter = StringUtils.startsWithAny(method.getName(), new String[] { "is", "get" }) && method	.getParameterTypes()
																														.isEmpty();
				boolean isSetter = StringUtils.startsWithAny(method.getName(), new String[] { "set" }) && method.getParameterTypes()
																												.size() == 1;

				return isGetter || isSetter;
			}

			private Method<T> determineWriteMethod(List<Method<T>> methods)
			{
				return methods	.stream()
								.filter(method -> method.getName()
														.startsWith("set"))
								.findFirst()
								.orElse(null);
			}

			private Method<T> determineReadMethod(List<Method<T>> methods)
			{
				return methods	.stream()
								.filter(method -> method.getName()
														.startsWith("get")
										|| method	.getName()
													.startsWith("is"))
								.findFirst()
								.orElse(null);
			}

			private String determinePropertyName(String methodName)
			{
				return StringUtils.uncapitalize(methodName	.replaceFirst("is", "")
															.replaceFirst("get", "")
															.replaceFirst("set", ""));
			}

			private Property<T> wrapMethodAsProperty(String property, Method<T> readMethod, Method<T> writeMethod)
			{
				return new Property<T>()
				{
					@Override
					public String getName()
					{
						return property;
					}

					@Override
					public AccessMethods<T> getAccessMethods()
					{
						return new AccessMethods<T>()
						{
							@Override
							public Method<T> getReadMethod()
							{
								return readMethod;
							}

							@Override
							public Method<T> getWriteMethod()
							{
								return writeMethod;
							}
						};
					}

					@Override
					public <E> PropertyAccessor<E> access(Class<E> propertyType, Supplier<T> instance)
					{
						return new PropertyAccessor<E>()
						{
							@Override
							public E get()
							{
								return readMethod	.access(instance.get())
													.getValue();
							}

							@Override
							public void set(E element)
							{
								writeMethod	.access(instance.get())
											.setValue(element);
							}

							@Override
							public <O> PropertyAccessor<O> as(Class<O> type)
							{
								return access(type, instance);
							}

							@Override
							public boolean hasPrimitiveType()
							{
								Class<?> type = this.getType();
								return org.omnaest.utils.ObjectUtils.isPrimitiveOrString(type);
							}

							@SuppressWarnings("unchecked")
							@Override
							public Class<E> getType()
							{
								return (Class<E>) ObjectUtils.defaultIfNull(readMethod != null ? readMethod.getReturnType() : null,
																			ListUtils.first(writeMethod != null ? writeMethod.getParameterTypes() : null));

							}
						};
					}

					@Override
					public <E> PropertyAccessor<E> access(Class<E> propertyType, T instance)
					{
						return this.access(propertyType, () -> instance);
					}

				};
			}

			@Override
			public PropertyAccessorMap accessAsMap(T instance)
			{
				return this.accessAsMap(() -> instance);
			}

			@Override
			public PropertyAccessorMap accessAsMap(Supplier<T> instance)
			{
				return new PropertyAccessorMapImpl(this	.getProperties()
														.collect(Collectors.toMap(	property -> property.getName(),
																					property -> (UnknownTypePropertyAccessor) property.access(	Object.class,
																																				instance))));
			}

			@Override
			public FlattenedPropertyAccessorMap accessAsFlattenedMap(T instance)
			{
				return this.accessAsFlattenedMap(() -> instance);
			}

			@Override
			public FlattenedPropertyAccessorMap accessAsFlattenedMap(Supplier<T> instance)
			{
				return new FlattenedPropertyAccessorMapImpl(this.accessAsMap(instance)
																.entrySet()
																.stream()
																.flatMap(entry ->
																{
																	String property = entry.getKey();

																	UnknownTypePropertyAccessor propertyAccessor = entry.getValue();

																	Stream<Map.Entry<List<String>, UnknownTypePropertyAccessor>> entries;

																	boolean hasPrimitiveType = propertyAccessor.hasPrimitiveType();
																	if (hasPrimitiveType)
																	{
																		Map.Entry<List<String>, UnknownTypePropertyAccessor> flattenedEntry = new Map.Entry<List<String>, UnknownTypePropertyAccessor>()
																		{
																			@Override
																			public List<String> getKey()
																			{
																				return Arrays.asList(property);
																			}

																			@Override
																			public UnknownTypePropertyAccessor getValue()
																			{
																				return propertyAccessor;
																			}

																			@Override
																			public UnknownTypePropertyAccessor setValue(UnknownTypePropertyAccessor value)
																			{
																				throw new UnsupportedOperationException();
																			}
																		};
																		entries = Stream.of(flattenedEntry);
																	}
																	else
																	{
																		@SuppressWarnings("unchecked")
																		Class<Object> propertyType = (Class<Object>) propertyAccessor.getType();

																		entries = BeanUtils	.analyze(propertyType)
																							.accessAsFlattenedMap(() -> propertyAccessor.as(Object.class)
																																		.get())
																							.entrySet()
																							.stream()
																							.map(subEntry ->
																							{
																								Map.Entry<List<String>, UnknownTypePropertyAccessor> retval = new Map.Entry<List<String>, UnknownTypePropertyAccessor>()
																								{
																									@Override
																									public List<String> getKey()
																									{
																										return ListUtils.addToNew(	subEntry.getKey(), 0,
																																	property);
																									}

																									@Override
																									public UnknownTypePropertyAccessor getValue()
																									{
																										return subEntry.getValue();
																									}

																									@Override
																									public UnknownTypePropertyAccessor setValue(UnknownTypePropertyAccessor value)
																									{
																										throw new UnsupportedOperationException();
																									}
																								};

																								return retval;
																							});
																	}

																	return entries;
																})
																.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
			}

		};
	}
}
