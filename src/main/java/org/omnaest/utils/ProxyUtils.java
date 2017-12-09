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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.ReflectionUtils.Method;
import org.omnaest.utils.ReflectionUtils.TypeReflection;

public class ProxyUtils
{
	public static interface ProxyBuilder
	{
		public <T> ProxyBuilderLoaded<T> of(Class<T> type);
	}

	public static interface Argument
	{
		public <E> E get();

		public <E> E getAs(Function<Object, E> mapper);

	}

	public static interface Arguments
	{
		public Argument at(int index);

		public Argument first();
	}

	private static class ArgumentsImpl implements Arguments
	{
		private Object[] args;

		public ArgumentsImpl(Object[] args)
		{
			super();
			this.args = args;
		}

		@Override
		public Argument first()
		{
			return this.at(0);
		}

		@Override
		public Argument at(int index)
		{
			return new Argument()
			{
				@Override
				public <E> E getAs(Function<Object, E> mapper)
				{
					return mapper.apply(ArgumentsImpl.this.args[index]);
				}

				@SuppressWarnings("unchecked")
				@Override
				public <E> E get()
				{
					return (E) (index >= 0 && ArgumentsImpl.this.args != null && index < ArgumentsImpl.this.args.length ? ArgumentsImpl.this.args[index]
							: null);
				}
			};
		}

		@Override
		public String toString()
		{
			return "ArgumentsImpl [args=" + Arrays.toString(this.args) + "]";
		}

	}

	public static interface MethodHandler
	{
		public Object handle(Arguments arguments) throws Exception;
	}

	public static interface MethodAndHandler
	{
		public Method<?> getMethod();

		public MethodHandler getMethodHandler();

		public default boolean hasHandler()
		{
			return this.getMethodHandler() != null;
		}
	}

	public static interface MethodHandlerBuilder
	{
		public MethodHandlerBuilder setHandler(MethodHandler methodHandler);

		public MethodHandlerBuilder setHandlerIf(Predicate<Method<?>> condition, MethodHandler methodHandler);

		public MethodAndHandler build();
	}

	public static interface ProxyBuilderLoaded<T> extends ProxyBuilder
	{
		public ProxyBuilderLoaded<T> withHandler(Predicate<Method<?>> methodMatcher, MethodHandler methodHandler);

		public ProxyBuilderLoaded<T> withHandler(Stream<MethodAndHandler> methodAndHandlers);

		public ProxyBuilderLoaded<T> withHandlers(Function<MethodHandlerBuilder, MethodAndHandler> builder);

		public T build();
	}

	public static ProxyBuilder builder()
	{
		return new ProxyBuilder()
		{
			@Override
			public <T> ProxyBuilderLoaded<T> of(Class<T> type)
			{
				ClassLoader classLoader = type.getClassLoader();
				return new ProxyBuilderLoaded<T>()
				{
					private List<Class<?>>									interfaceTypes	= new ArrayList<>();
					private List<Method<?>>									methods			= new ArrayList<>();
					private Map<java.lang.reflect.Method, MethodHandler>	handlers		= new LinkedHashMap<>();

					@SuppressWarnings("unchecked")
					@Override
					public <T2> ProxyBuilderLoaded<T2> of(Class<T2> type)
					{
						TypeReflection<T2> typeReflection = ReflectionUtils.of(type);

						this.methods.addAll(typeReflection	.getMethods()
															.collect(Collectors.toList()));

						this.interfaceTypes.add(type);
						return (ProxyBuilderLoaded<T2>) this;
					}

					@Override
					public ProxyBuilderLoaded<T> withHandler(Predicate<Method<?>> methodMatcher, MethodHandler methodHandler)
					{
						this.handlers.putAll(this.methods	.stream()
															.filter(methodMatcher)
															.map(method -> method.getRawMethod())
															.collect(Collectors.toMap(method -> method, method -> methodHandler)));
						return this;
					}

					@Override
					public ProxyBuilderLoaded<T> withHandler(Stream<MethodAndHandler> methodAndHandlers)
					{
						this.handlers.putAll(methodAndHandlers	.filter(mah -> mah != null)
																.collect(Collectors.toMap(	mah -> mah	.getMethod()
																										.getRawMethod(),
																							mah -> mah.getMethodHandler())));
						return this;
					}

					@Override
					public ProxyBuilderLoaded<T> withHandlers(Function<MethodHandlerBuilder, MethodAndHandler> builder)
					{
						this.methods.stream()
									.map(method -> (MethodHandlerBuilder) new MethodHandlerBuilder()
									{
										private MethodHandler methodHandler;

										@Override
										public MethodHandlerBuilder setHandler(MethodHandler methodHandler)
										{
											this.methodHandler = methodHandler;
											return this;
										}

										@Override
										public MethodHandlerBuilder setHandlerIf(Predicate<Method<?>> condition, MethodHandler methodHandler)
										{
											if (condition.test(method))
											{
												this.setHandler(methodHandler);
											}
											return this;
										}

										@Override
										public MethodAndHandler build()
										{
											return new MethodAndHandler()
											{
												@Override
												public MethodHandler getMethodHandler()
												{
													return methodHandler;
												}

												@Override
												public Method<?> getMethod()
												{
													return method;
												}

												@Override
												public boolean hasHandler()
												{
													return this.getMethodHandler() != null;
												}

											};
										}
									})
									.map(builder)
									.filter(handler -> handler != null)
									.filter(MethodAndHandler::hasHandler)
									.forEach(methodAndHandler ->
									{
										this.handlers.put(	methodAndHandler.getMethod()
																			.getRawMethod(),
															methodAndHandler.getMethodHandler());
									});
						return this;
					}

					@SuppressWarnings("unchecked")
					@Override
					public T build()
					{
						InvocationHandler invocationHandler = (proxy, method, args) ->
						{
							Object retval = null;

							MethodHandler methodHandler = this.handlers.get(method);
							if (methodHandler != null)
							{
								retval = methodHandler.handle(new ArgumentsImpl(args));
							}

							return retval;
						};
						return (T) Proxy.newProxyInstance(classLoader, this.interfaceTypes.toArray(new Class[0]), invocationHandler);
					}
				}.of(type);
			}
		};
	}

}
