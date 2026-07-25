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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.omnaest.utils.ReflectionUtils.Method;
import org.omnaest.utils.ReflectionUtils.TypeReflection;

import lombok.RequiredArgsConstructor;

public class ProxyUtils
{
    public static interface ProxyBuilder
    {
        public <T> ProxyBuilderLoaded<T> of(Class<T> type);
    }

    public static interface Argument
    {
        /**
         * Returns the argument value or null if there is no argument at the underlying index.
         */
        public <E> E get();

        /**
         * Returns the argument value mapped by the given {@link Function}. The mapper is invoked with null if there is no argument at the underlying index.
         */
        public <E> E getAs(Function<Object, E> mapper);

        /**
         * Returns the declared parameter type or null if there is no argument at the underlying index.
         */
        public Class<?> getType();

        /**
         * Returns the argument value as {@link String} or null if the value is null.
         */
        public String getAsString();

        /**
         * Returns the argument value as {@link Boolean} or null if the value is null or not interpretable as {@link Boolean}. A {@link Number} is true if it is
         * not zero, a {@link CharSequence} is interpreted like e.g. "true", "yes", "on" or "1".
         */
        public Boolean getAsBoolean();

        /**
         * Returns the argument value as {@link Character} or null if the value is null or an empty {@link CharSequence}. For a longer {@link CharSequence} the
         * first character is returned.
         */
        public Character getAsCharacter();

        /**
         * @see #getAsInteger()
         */
        public Byte getAsByte();

        /**
         * @see #getAsInteger()
         */
        public Short getAsShort();

        /**
         * Returns the argument value as {@link Integer} or null if the value is null. A {@link Number} is narrowed or widened, any other value is parsed from
         * its {@link String} representation, which throws a {@link NumberFormatException} if it is not a number.
         */
        public Integer getAsInteger();

        /**
         * @see #getAsInteger()
         */
        public Long getAsLong();

        /**
         * @see #getAsInteger()
         */
        public Float getAsFloat();

        /**
         * @see #getAsInteger()
         */
        public Double getAsDouble();

        /**
         * @see #getAsInteger()
         */
        public BigInteger getAsBigInteger();

        /**
         * @see #getAsInteger()
         */
        public BigDecimal getAsBigDecimal();

        /**
         * Returns the argument value as {@link Enum} value of the given {@link Enum} type or null if the value is null or does not match any {@link Enum}
         * constant by name.
         */
        public <E extends Enum<E>> E getAsEnum(Class<E> enumType);

    }

    public static interface Arguments
    {
        public Argument at(int index);

        public Argument first();

        public Stream<Argument> stream();

        public int size();
    }

    @RequiredArgsConstructor
    private static class ArgumentImpl implements Argument
    {
        private final Object   value;
        private final Class<?> type;

        @SuppressWarnings("unchecked")
        @Override
        public <E> E get()
        {
            return (E) this.value;
        }

        @Override
        public <E> E getAs(Function<Object, E> mapper)
        {
            return mapper.apply(this.value);
        }

        @Override
        public Class<?> getType()
        {
            return this.type;
        }

        @Override
        public String getAsString()
        {
            return this.value != null ? String.valueOf(this.value) : null;
        }

        @Override
        public Boolean getAsBoolean()
        {
            if (this.value == null)
            {
                return null;
            }
            else if (this.value instanceof Boolean)
            {
                return (Boolean) this.value;
            }
            else if (this.value instanceof Number)
            {
                return ((Number) this.value).doubleValue() != 0.0;
            }
            else
            {
                return org.apache.commons.lang3.BooleanUtils.toBooleanObject(this.determineTrimmedString());
            }
        }

        @Override
        public Character getAsCharacter()
        {
            if (this.value == null)
            {
                return null;
            }
            else if (this.value instanceof Character)
            {
                return (Character) this.value;
            }
            else
            {
                String token = String.valueOf(this.value);
                return token.isEmpty() ? null : token.charAt(0);
            }
        }

        @Override
        public Byte getAsByte()
        {
            return this.convertNumber(Number::byteValue, Byte::valueOf);
        }

        @Override
        public Short getAsShort()
        {
            return this.convertNumber(Number::shortValue, Short::valueOf);
        }

        @Override
        public Integer getAsInteger()
        {
            return this.convertNumber(Number::intValue, Integer::valueOf);
        }

        @Override
        public Long getAsLong()
        {
            return this.convertNumber(Number::longValue, Long::valueOf);
        }

        @Override
        public Float getAsFloat()
        {
            return this.convertNumber(Number::floatValue, Float::valueOf);
        }

        @Override
        public Double getAsDouble()
        {
            return this.convertNumber(Number::doubleValue, Double::valueOf);
        }

        @Override
        public BigDecimal getAsBigDecimal()
        {
            if (this.value == null)
            {
                return null;
            }
            else if (this.value instanceof BigDecimal)
            {
                return (BigDecimal) this.value;
            }
            else
            {
                //
                // the String representation is used even for a Number, since e.g. new BigDecimal(0.1d) would not be exact
                return new BigDecimal(this.determineTrimmedString());
            }
        }

        @Override
        public BigInteger getAsBigInteger()
        {
            BigDecimal bigDecimal = this.getAsBigDecimal();
            return bigDecimal != null ? bigDecimal.toBigInteger() : null;
        }

        @Override
        public <E extends Enum<E>> E getAsEnum(Class<E> enumType)
        {
            if (this.value == null)
            {
                return null;
            }
            else if (enumType.isInstance(this.value))
            {
                return enumType.cast(this.value);
            }
            else
            {
                return EnumUtils.toEnumValue(this.determineTrimmedString(), enumType)
                                .orElse(null);
            }
        }

        private <E> E convertNumber(Function<Number, E> numberMapper, Function<String, E> stringParser)
        {
            if (this.value == null)
            {
                return null;
            }
            else if (this.value instanceof Number)
            {
                return numberMapper.apply((Number) this.value);
            }
            else
            {
                return stringParser.apply(this.determineTrimmedString());
            }
        }

        private String determineTrimmedString()
        {
            return String.valueOf(this.value)
                         .trim();
        }

        @Override
        public String toString()
        {
            return "ArgumentImpl [value=" + this.value + ", type=" + this.type + "]";
        }

    }

    @RequiredArgsConstructor
    private static class ArgumentsImpl implements Arguments
    {
        private final Object[]   arguments;
        private final Class<?>[] types;

        @Override
        public Argument first()
        {
            return this.at(0);
        }

        @Override
        public Argument at(int index)
        {
            return new ArgumentImpl(this.determineValue(index), this.determineType(index));
        }

        private Object determineValue(int index)
        {
            return this.isValidIndex(index, this.arguments) ? this.arguments[index] : null;
        }

        private Class<?> determineType(int index)
        {
            return this.isValidIndex(index, this.types) ? this.types[index] : null;
        }

        private boolean isValidIndex(int index, Object[] array)
        {
            return array != null && index >= 0 && index < array.length;
        }

        @Override
        public int size()
        {
            return this.arguments != null ? this.arguments.length : 0;
        }

        @Override
        public Stream<Argument> stream()
        {
            return IntStream.range(0, this.size())
                            .mapToObj(this::at);
        }

        @Override
        public String toString()
        {
            return "ArgumentsImpl [args=" + Arrays.toString(this.arguments) + "]";
        }

    }

    /**
     * Similar to {@link MethodHandler} but provides information about the {@link InvocationMethod} in addition.
     * <br>
     * <br>
     * A thrown {@link Exception} is passed to the caller of the proxied method as is, as long as the method declares it. Any other checked
     * {@link Exception} is wrapped into a {@link java.lang.reflect.UndeclaredThrowableException} by the {@link Proxy} runtime.
     *
     * @author omnaest
     */
    public static interface MethodInvocationHandler
    {
        public Object handle(InvocationMethod method, Arguments arguments) throws Exception;
    }

    /**
     * Handler of a proxied method invocation which is only interested in the {@link Arguments}. See {@link MethodInvocationHandler} for the exception
     * semantics.
     *
     * @author omnaest
     */
    public static interface MethodHandler
    {
        public Object handle(Arguments arguments) throws Exception;
    }

    private static MethodInvocationHandler toMethodInvocationHandler(MethodHandler methodHandler)
    {
        return methodHandler != null ? new MethodHandlerToMethodInvocationHandlerAdapter(methodHandler) : null;
    }

    private static class MethodHandlerToMethodInvocationHandlerAdapter implements MethodInvocationHandler
    {
        private final MethodHandler methodHandler;

        private MethodHandlerToMethodInvocationHandlerAdapter(MethodHandler methodHandler)
        {
            this.methodHandler = methodHandler;
        }

        @Override
        public Object handle(InvocationMethod method, Arguments arguments) throws Exception
        {
            return this.methodHandler.handle(arguments);
        }
    }

    public static interface InvocationMethod
    {
        public String getName();

        public Class<?> getReturnType();

        public List<Class<?>> getArgumentTypes();

    }

    public static interface MethodAndHandler
    {
        public Method<?> getMethod();

        public MethodHandler getMethodHandler();
    }

    private static boolean hasHandler(MethodAndHandler methodAndHandler)
    {
        return methodAndHandler != null && methodAndHandler.getMethodHandler() != null;
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

        public ProxyBuilderLoaded<T> withHandler(Predicate<Method<?>> methodMatcher, MethodInvocationHandler methodInvocationHandler);

        public ProxyBuilderLoaded<T> withHandler(Stream<MethodAndHandler> methodAndHandlers);

        public ProxyBuilderLoaded<T> withHandlers(Function<MethodHandlerBuilder, MethodAndHandler> builder);

        /**
         * Handler being invoked for all methods without an explicitly registered handler. Without a default handler such a method invocation throws an
         * {@link UnsupportedOperationException}. Use <code>withDefaultHandler(arguments -> null)</code> to get a proxy which silently returns null instead.
         * <br>
         * <br>
         * {@link Object#hashCode()}, {@link Object#equals(Object)}, {@link Object#toString()} and <code>default</code> methods are never routed to the default
         * handler, only explicitly registered handlers do override those.
         */
        public ProxyBuilderLoaded<T> withDefaultHandler(MethodHandler methodHandler);

        /**
         * Similar to {@link #withDefaultHandler(MethodHandler)} but with access to the {@link InvocationMethod}.
         */
        public ProxyBuilderLoaded<T> withDefaultHandler(MethodInvocationHandler methodInvocationHandler);

        public T build();
    }

    public static ProxyBuilder builder()
    {
        return new ProxyBuilder() {
            @Override
            public <T> ProxyBuilderLoaded<T> of(Class<T> type)
            {
                return new ProxyBuilderLoadedImpl<T>().of(type);
            }
        };
    }

    private static class ProxyBuilderLoadedImpl<T> implements ProxyBuilderLoaded<T>
    {
        private final List<Class<?>>                                         interfaceTypes = new ArrayList<>();
        private final List<Method<?>>                                        methods        = new ArrayList<>();
        private final Map<java.lang.reflect.Method, MethodInvocationHandler> handlers       = new LinkedHashMap<>();

        private MethodInvocationHandler defaultHandler;

        @SuppressWarnings("unchecked")
        @Override
        public <T2> ProxyBuilderLoaded<T2> of(Class<T2> type)
        {
            if (type == null)
            {
                throw new IllegalArgumentException("Proxy type must not be null");
            }
            if (!type.isInterface())
            {
                throw new IllegalArgumentException("Proxy type must be an interface but was: " + type.getName());
            }

            if (!this.interfaceTypes.contains(type))
            {
                TypeReflection<T2> typeReflection = ReflectionUtils.of(type);

                this.methods.addAll(typeReflection.getMethods()
                                                  .collect(Collectors.toList()));

                this.interfaceTypes.add(type);
            }

            return (ProxyBuilderLoaded<T2>) this;
        }

        @Override
        public ProxyBuilderLoaded<T> withHandler(Predicate<Method<?>> methodMatcher, MethodInvocationHandler methodInvocationHandler)
        {
            this.methods.stream()
                        .filter(methodMatcher)
                        .map(method -> method.getRawMethod())
                        .forEach(method -> this.handlers.put(method, methodInvocationHandler));
            return this;
        }

        @Override
        public ProxyBuilderLoaded<T> withHandler(Predicate<Method<?>> methodMatcher, MethodHandler methodHandler)
        {
            return this.withHandler(methodMatcher, toMethodInvocationHandler(methodHandler));
        }

        @Override
        public ProxyBuilderLoaded<T> withHandler(Stream<MethodAndHandler> methodAndHandlers)
        {
            methodAndHandlers.filter(ProxyUtils::hasHandler)
                             .forEach(this::registerMethodAndHandler);
            return this;
        }

        @Override
        public ProxyBuilderLoaded<T> withHandlers(Function<MethodHandlerBuilder, MethodAndHandler> builder)
        {
            this.methods.stream()
                        .map(method -> (MethodHandlerBuilder) new MethodHandlerBuilder() {
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
                                return new MethodAndHandler() {
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

                                };
                            }
                        })
                        .map(builder)
                        .filter(ProxyUtils::hasHandler)
                        .forEach(this::registerMethodAndHandler);
            return this;
        }

        @Override
        public ProxyBuilderLoaded<T> withDefaultHandler(MethodInvocationHandler methodInvocationHandler)
        {
            this.defaultHandler = methodInvocationHandler;
            return this;
        }

        @Override
        public ProxyBuilderLoaded<T> withDefaultHandler(MethodHandler methodHandler)
        {
            return this.withDefaultHandler(toMethodInvocationHandler(methodHandler));
        }

        private void registerMethodAndHandler(MethodAndHandler methodAndHandler)
        {
            this.handlers.put(methodAndHandler.getMethod()
                                              .getRawMethod(),
                              toMethodInvocationHandler(methodAndHandler.getMethodHandler()));
        }

        @SuppressWarnings("unchecked")
        @Override
        public T build()
        {
            Map<java.lang.reflect.Method, MethodInvocationHandler> handlers = new LinkedHashMap<>(this.handlers);
            MethodInvocationHandler defaultHandler = this.defaultHandler;
            Class<?>[] interfaceTypes = this.interfaceTypes.toArray(new Class<?>[0]);
            ClassLoader classLoader = this.determineClassLoader();

            InvocationHandler invocationHandler = (proxy, method, args) ->
            {
                MethodInvocationHandler methodInvocationHandler = handlers.get(method);
                if (methodInvocationHandler != null)
                {
                    return methodInvocationHandler.handle(createInvocationMethod(method), new ArgumentsImpl(args, method.getParameterTypes()));
                }
                else if (isObjectMethod(method))
                {
                    return handleObjectMethod(proxy, method, args, interfaceTypes);
                }
                else if (method.isDefault())
                {
                    return InvocationHandler.invokeDefault(proxy, method, args != null ? args : new Object[0]);
                }
                else if (defaultHandler != null)
                {
                    return defaultHandler.handle(createInvocationMethod(method), new ArgumentsImpl(args, method.getParameterTypes()));
                }
                else
                {
                    throw new UnsupportedOperationException("No handler registered for proxied method: " + method);
                }
            };
            return (T) Proxy.newProxyInstance(classLoader, interfaceTypes, invocationHandler);
        }

        /**
         * Determines a {@link ClassLoader} which is able to see all proxied interfaces, since {@link Proxy#newProxyInstance(ClassLoader, Class[],
         * InvocationHandler)} requires all of them to be visible from the single given {@link ClassLoader}.
         */
        private ClassLoader determineClassLoader()
        {
            List<ClassLoader> candidates = new ArrayList<>();
            this.interfaceTypes.forEach(interfaceType -> candidates.add(interfaceType.getClassLoader()));
            candidates.add(Thread.currentThread()
                                 .getContextClassLoader());
            candidates.add(ProxyUtils.class.getClassLoader());

            //
            // no Stream::findFirst here, since the bootstrap ClassLoader is represented by null
            for (ClassLoader candidate : candidates)
            {
                if (this.canSeeAllInterfaceTypes(candidate))
                {
                    return candidate;
                }
            }
            return ProxyUtils.class.getClassLoader();
        }

        private boolean canSeeAllInterfaceTypes(ClassLoader classLoader)
        {
            return this.interfaceTypes.stream()
                                      .allMatch(interfaceType -> this.isVisible(classLoader, interfaceType));
        }

        private boolean isVisible(ClassLoader classLoader, Class<?> type)
        {
            try
            {
                return Class.forName(type.getName(), false, classLoader) == type;
            }
            catch (ClassNotFoundException | LinkageError e)
            {
                return false;
            }
        }

    }

    /**
     * Returns true if the given {@link java.lang.reflect.Method} has the signature of one of the {@link Object} methods a {@link Proxy} routes to its
     * {@link InvocationHandler}.
     */
    private static boolean isObjectMethod(java.lang.reflect.Method method)
    {
        String name = method.getName();
        int parameterCount = method.getParameterCount();
        return (parameterCount == 0 && ("hashCode".equals(name) || "toString".equals(name)))
                || (parameterCount == 1 && "equals".equals(name) && Object.class.equals(method.getParameterTypes()[0]));
    }

    /**
     * Provides identity based {@link Object#hashCode()}, {@link Object#equals(Object)} and {@link Object#toString()} semantics for a proxy without an
     * explicitly registered handler. Without this a {@link Proxy} would return null for those and e.g. break any {@link Map} or {@link java.util.Set} the
     * proxy is put into.
     */
    private static Object handleObjectMethod(Object proxy, java.lang.reflect.Method method, Object[] args, Class<?>[] interfaceTypes)
    {
        String name = method.getName();
        if ("hashCode".equals(name))
        {
            return System.identityHashCode(proxy);
        }
        else if ("equals".equals(name))
        {
            return proxy == (args != null && args.length > 0 ? args[0] : null);
        }
        else
        {
            return "Proxy [interfaces=" + Arrays.stream(interfaceTypes)
                                                .map(Class::getName)
                                                .collect(Collectors.joining(", ", "[", "]"))
                    + ", identity=" + Integer.toHexString(System.identityHashCode(proxy)) + "]";
        }
    }

    private static InvocationMethod createInvocationMethod(java.lang.reflect.Method method)
    {
        return new InvocationMethod() {

            @Override
            public String getName()
            {
                return method.getName();
            }

            @Override
            public Class<?> getReturnType()
            {
                return method.getReturnType();
            }

            @Override
            public List<Class<?>> getArgumentTypes()
            {
                return Arrays.asList(method.getParameterTypes());
            }

        };
    }

}
