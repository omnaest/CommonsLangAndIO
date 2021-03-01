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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.omnaest.utils.ProxyUtils.MethodAndHandler;
import org.omnaest.utils.ProxyUtils.MethodHandler;
import org.omnaest.utils.ReflectionUtils.Field;
import org.omnaest.utils.ReflectionUtils.Method;
import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.map.MapDecorator;

public class BeanUtils
{
    static class FlattenedPropertyImpl extends AbstractFlattenedProperty
    {
        private Property<Object> property;

        @SuppressWarnings("unchecked")
        public FlattenedPropertyImpl(Property<?> property)
        {
            this.property = (Property<Object>) property;
        }

        @Override
        public Property<Object> getProperty()
        {
            return this.property;
        }

        @Override
        public List<String> getNestedName()
        {
            return Arrays.asList(this.property.getName());
        }

        @Override
        public FlattenedProperty getParentProperty()
        {
            //root return null
            return null;
        }

        @Override
        public Stream<NestedFlattenedProperty> getSubProperties()
        {
            boolean hasPrimitiveType = this.property.hasPrimitiveType();
            return hasPrimitiveType ? Stream.empty()
                    : BeanUtils.analyze(this.property.getType())
                               .getNestedFlattenedProperties()
                               .map(flattenedProperty -> this.wrapSubNestedProperty(this, flattenedProperty));
        }

        private <T> NestedFlattenedProperty wrapSubNestedProperty(FlattenedProperty parentProperty, NestedFlattenedProperty flattenedProperty)
        {
            return new AbstractFlattenedProperty()
            {
                @Override
                public Property<Object> getProperty()
                {
                    return flattenedProperty.getProperty();
                }

                @Override
                public FlattenedProperty getParentProperty()
                {
                    return parentProperty;
                }

                @Override
                public List<String> getNestedName()
                {
                    return Stream.concat(parentProperty.getNestedName()
                                                       .stream(),
                                         flattenedProperty.getNestedName()
                                                          .stream())
                                 .collect(Collectors.toList());
                }

                @Override
                public Stream<NestedFlattenedProperty> getSubProperties()
                {
                    return flattenedProperty.getSubProperties()
                                            .map(subProperty -> FlattenedPropertyImpl.this.wrapSubNestedProperty(this, subProperty));
                }
            };
        }
    }

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

        public PropertyAccessor<Object> access(T instance);

        public <E> PropertyAccessor<E> access(Class<E> propertyType, Supplier<T> instance);

        public Class<?> getType();

        public boolean hasPrimitiveType();

        public <A extends Annotation> Stream<A> getAnnotations(Class<A> annotationType);
    }

    public static interface FlattenedProperty
    {
        public List<String> getNestedName();

        public FlattenedProperty getParentProperty();

        public String getPropertyName();

        public Property<Object> getProperty();
    }

    public static interface NestedFlattenedProperty extends FlattenedProperty
    {
        public Stream<NestedFlattenedProperty> getSubProperties();

        public <E, T> PropertyAccessor<E> accessFromRoot(Class<E> propertyType, T instance);

        List<FlattenedProperty> getParentPropertiesAndThis();

        List<FlattenedProperty> getParentProperties();
    }

    static abstract class AbstractFlattenedProperty implements NestedFlattenedProperty
    {
        @Override
        public String getPropertyName()
        {
            return this.getNestedName()
                       .stream()
                       .collect(Collectors.joining("."));
        }

        @Override
        public int hashCode()
        {
            return this.getNestedName()
                       .hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            return this.getNestedName()
                       .equals(obj);
        }

        @Override
        public String toString()
        {
            return this.getNestedName()
                       .toString();
        }

        @Override
        public List<FlattenedProperty> getParentProperties()
        {
            List<FlattenedProperty> retlist = new ArrayList<>();

            FlattenedProperty parentProperty = this;
            do
            {
                parentProperty = parentProperty.getParentProperty();
                if (parentProperty != null)
                {
                    retlist.add(0, parentProperty);
                }
            } while (parentProperty != null);
            return retlist;
        }

        @Override
        public List<FlattenedProperty> getParentPropertiesAndThis()
        {
            return ListUtils.mergedList(this.getParentProperties(), Arrays.asList(this));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E, T> PropertyAccessor<E> accessFromRoot(Class<E> propertyType, T instance)
        {
            PropertyAccessor<?> retval = null;

            Object currentInstance = instance;
            for (FlattenedProperty property : this.getParentPropertiesAndThis())
            {
                retval = property.getProperty()
                                 .access(currentInstance);
                currentInstance = retval.get();
            }

            return (PropertyAccessor<E>) retval;
        }

    }

    public static interface InstanceAccessor<T>
    {
        public PropertyAccessorMap asMap();

        public FlattenedPropertyAccessorMap asFlattenedMap();
    }

    public static interface BeanAnalyzer<T>
    {
        /**
         * Gets the {@link Property} of the given {@link Class} type
         * 
         * @return
         */
        public Stream<Property<T>> getProperties();

        /**
         * Returns the {@link NestedFlattenedProperty} for the root level, but allows to retrieve any child level via
         * {@link NestedFlattenedProperty#getSubProperties()}
         * 
         * @return
         */
        public Stream<NestedFlattenedProperty> getNestedFlattenedProperties();

        /**
         * Similar to {@link #getNestedFlattenedProperties()} with a fully expanded property tree. Be aware that this will fail for cyclic references in the
         * type tree!
         * 
         * @return
         */
        public Stream<FlattenedProperty> getFlattenedProperties();

        public InstanceAccessor<T> access(T instance);

        public InstanceAccessor<T> access(Supplier<T> instance);

        public T newProxy(Map<String, ? extends BeanPropertyAccessor<?>> propertyToAccessor);

        public T newProxy(Consumer<BeanPropertyAccessorRegistry> registerStream);

        public MapProxyFactory<T> toMapProxyFactory();

        public <A extends Annotation> Stream<A> resolveTypeAnnotation(Class<A> annotationType);

        /**
         * Returns the underlying {@link Class} type
         * 
         * @return
         */
        public Class<T> getType();
    }

    public static interface MapProxyFactory<T> extends Function<Map<String, ? extends Object>, T>
    {

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
            return this.entrySet()
                       .stream()
                       .collect(Collectors.toMap(entry -> entry.getKey()
                                                               .stream()
                                                               .collect(Collectors.joining(".")),
                                                 entry ->
                                                 {
                                                     UnknownTypePropertyAccessor propertyAccessor = entry.getValue();
                                                     return propertyAccessor.as(String.class)
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

    private static class CachedBeanAnalyzer<T> implements BeanAnalyzer<T>
    {
        private BeanAnalyzer<T> analyzer;

        private CachedElement<List<Property<T>>>             properties                = CachedElement.of(() -> this.analyzer.getProperties()
                                                                                                                             .collect(Collectors.toList()));
        private CachedElement<List<FlattenedProperty>>       flattenedProperties       = CachedElement.of(() -> this.analyzer.getFlattenedProperties()
                                                                                                                             .collect(Collectors.toList()));
        private CachedElement<List<NestedFlattenedProperty>> nestedFlattenedProperties = CachedElement.of(() -> this.analyzer.getNestedFlattenedProperties()
                                                                                                                             .collect(Collectors.toList()));

        public CachedBeanAnalyzer(BeanAnalyzer<T> analyzer)
        {
            super();
            this.analyzer = analyzer;
        }

        @Override
        public Stream<Property<T>> getProperties()
        {
            return this.properties.get()
                                  .stream();
        }

        @Override
        public Stream<NestedFlattenedProperty> getNestedFlattenedProperties()
        {
            return this.nestedFlattenedProperties.get()
                                                 .stream();
        }

        @Override
        public Stream<FlattenedProperty> getFlattenedProperties()
        {
            return this.flattenedProperties.get()
                                           .stream();
        }

        @Override
        public InstanceAccessor<T> access(T instance)
        {
            return this.analyzer.access(instance);
        }

        @Override
        public InstanceAccessor<T> access(Supplier<T> instance)
        {
            return this.analyzer.access(instance);
        }

        @Override
        public T newProxy(Map<String, ? extends BeanPropertyAccessor<?>> propertyToAccessor)
        {
            return this.analyzer.newProxy(propertyToAccessor);
        }

        @Override
        public T newProxy(Consumer<BeanPropertyAccessorRegistry> registerStream)
        {
            return this.analyzer.newProxy(registerStream);
        }

        @Override
        public MapProxyFactory<T> toMapProxyFactory()
        {
            return this.analyzer.toMapProxyFactory();
        }

        @Override
        public <A extends Annotation> Stream<A> resolveTypeAnnotation(Class<A> annotationType)
        {
            return this.analyzer.resolveTypeAnnotation(annotationType);
        }

        @Override
        public Class<T> getType()
        {
            return this.analyzer.getType();
        }

    }

    public static <T> BeanAnalyzer<T> analyze(Class<T> type)
    {
        return new CachedBeanAnalyzer<>(new BeanAnalyzer<T>()
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
                return ProxyUtils.builder()
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
                                                                                     beanPropertyAccessor.accept(arguments.first()
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
            public MapProxyFactory<T> toMapProxyFactory()
            {
                return new MapProxyFactory<T>()
                {
                    @Override
                    public T apply(Map<String, ? extends Object> map)
                    {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> referenceMap = (Map<String, Object>) map;
                        T proxy = newProxy(registry -> registry.attach(new BeanPropertyAccessor<Object>()
                        {
                            private String property = registry.getProperty()
                                                              .getName();

                            @Override
                            public void accept(Object value)
                            {
                                referenceMap.put(this.property, value);
                            }

                            @Override
                            public Object get()
                            {
                                return map.get(this.property);
                            }
                        }));
                        return proxy;
                    }
                };
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
                                        .map(entry -> this.wrapMethodAsProperty(entry.getKey(), this.determineField(entry.getKey()),
                                                                                this.determineReadMethod(entry.getValue()),
                                                                                this.determineWriteMethod(entry.getValue())));
            }

            private Field<T> determineField(String fieldName)
            {
                return ReflectionUtils.of(type)
                                      .getField(fieldName)
                                      .orElse(null);
            }

            @Override
            public Stream<NestedFlattenedProperty> getNestedFlattenedProperties()
            {
                return this.getProperties()
                           .map(FlattenedPropertyImpl::new);
            }

            @Override
            public Stream<FlattenedProperty> getFlattenedProperties()
            {
                return this.getNestedFlattenedProperties()
                           .flatMap(property -> Stream.concat(Stream.of(property), property.getSubProperties()));
            }

            private boolean isPropertyMethod(Method<T> method)
            {
                boolean isGetter = StringUtils.startsWithAny(method.getName(), new String[] { "is", "get" }) && method.getParameterTypes()
                                                                                                                      .isEmpty();
                boolean isSetter = StringUtils.startsWithAny(method.getName(), new String[] { "set" }) && method.getParameterTypes()
                                                                                                                .size() == 1;

                boolean isJavaInternalMethod = method.getName()
                                                     .equals("getClass");
                return !isJavaInternalMethod && (isGetter || isSetter);
            }

            private Method<T> determineWriteMethod(List<Method<T>> methods)
            {
                return methods.stream()
                              .filter(method -> method.getName()
                                                      .startsWith("set"))
                              .findFirst()
                              .orElse(null);
            }

            private Method<T> determineReadMethod(List<Method<T>> methods)
            {
                return methods.stream()
                              .filter(method -> method.getName()
                                                      .startsWith("get")
                                      || method.getName()
                                               .startsWith("is"))
                              .findFirst()
                              .orElse(null);
            }

            private String determinePropertyName(String methodName)
            {
                return StringUtils.uncapitalize(methodName.replaceFirst("is", "")
                                                          .replaceFirst("get", "")
                                                          .replaceFirst("set", ""));
            }

            private Property<T> wrapMethodAsProperty(String property, Field<T> field, Method<T> readMethod, Method<T> writeMethod)
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
                    public Class<?> getType()
                    {
                        Class<?> getterType = readMethod != null ? readMethod.getReturnType() : null;
                        Class<?> setterType = writeMethod != null && !writeMethod.getParameterTypes()
                                                                                 .isEmpty() ? writeMethod.getParameterTypes()
                                                                                                         .get(0)
                                                                                         : null;
                        return org.apache.commons.lang3.ObjectUtils.defaultIfNull(getterType, setterType);
                    }

                    @Override
                    public boolean hasPrimitiveType()
                    {
                        return org.omnaest.utils.ObjectUtils.isPrimitiveOrString(this.getType());
                    }

                    @Override
                    public PropertyAccessor<Object> access(T instance)
                    {
                        return this.access(Object.class, instance);
                    }

                    @Override
                    public <E> PropertyAccessor<E> access(Class<E> propertyType, Supplier<T> instance)
                    {
                        return new PropertyAccessor<E>()
                        {
                            @Override
                            public E get()
                            {
                                return readMethod.access(instance.get())
                                                 .getValue();
                            }

                            @Override
                            public void set(E element)
                            {
                                writeMethod.access(instance.get())
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

                    @Override
                    public <A extends Annotation> Stream<A> getAnnotations(Class<A> annotationType)
                    {
                        return StreamUtils.concat(Optional.ofNullable(field)
                                                          .map(field ->
                                                          {
                                                              return field.getAnnotations(annotationType);
                                                          })
                                                          .orElse(Stream.empty()),
                                                  Optional.ofNullable(readMethod)
                                                          .map(method -> method.getAnnotations(annotationType))
                                                          .orElse(Stream.empty()),
                                                  Optional.ofNullable(writeMethod)
                                                          .map(method -> method.getAnnotations(annotationType))
                                                          .orElse(Stream.empty()))
                                          .distinct();
                    }

                };
            }

            @Override
            public InstanceAccessor<T> access(T instance)
            {
                return this.access(() -> instance);
            }

            @Override
            public InstanceAccessor<T> access(Supplier<T> instance)
            {
                return new InstanceAccessor<T>()
                {
                    private CachedElement<PropertyAccessorMap>          propertyAccessorMap          = CachedElement.of(() -> this.asMapUncached());
                    private CachedElement<FlattenedPropertyAccessorMap> flattenedPropertyAccessorMap = CachedElement.of(() -> this.asFlattenedMapUncached());

                    @Override
                    public PropertyAccessorMap asMap()
                    {
                        return this.propertyAccessorMap.get();
                    }

                    private PropertyAccessorMap asMapUncached()
                    {
                        return new PropertyAccessorMapImpl(getProperties().collect(Collectors.toMap(property -> property.getName(),
                                                                                                    property -> (UnknownTypePropertyAccessor) property.access(Object.class,
                                                                                                                                                              instance))));
                    }

                    @Override
                    public FlattenedPropertyAccessorMap asFlattenedMap()
                    {
                        return this.flattenedPropertyAccessorMap.get();
                    }

                    private FlattenedPropertyAccessorMap asFlattenedMapUncached()
                    {
                        return new FlattenedPropertyAccessorMapImpl(this.asMap()
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

                                                                                entries = BeanUtils.analyze(propertyType)
                                                                                                   .access(() -> propertyAccessor.as(Object.class)
                                                                                                                                 .get())
                                                                                                   .asFlattenedMap()
                                                                                                   .entrySet()
                                                                                                   .stream()
                                                                                                   .map(subEntry ->
                                                                                                   {
                                                                                                       Map.Entry<List<String>, UnknownTypePropertyAccessor> retval = new Map.Entry<List<String>, UnknownTypePropertyAccessor>()
                                                                                                       {
                                                                                                           @Override
                                                                                                           public List<String> getKey()
                                                                                                           {
                                                                                                               return ListUtils.addToNew(subEntry.getKey(), 0,
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

            @Override
            public <A extends Annotation> Stream<A> resolveTypeAnnotation(Class<A> annotationType)
            {
                return Stream.of(type.getAnnotationsByType(annotationType));
            }

            @Override
            public Class<T> getType()
            {
                return type;
            }
        });

    }

}
