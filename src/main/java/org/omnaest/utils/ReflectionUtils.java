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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectionUtils
{
    private static class FieldReflectionImpl<T> implements FieldReflection, FieldReflectionTyped<T>
    {
        private java.lang.reflect.Field field;

        private FieldReflectionImpl(java.lang.reflect.Field field)
        {
            this.field = field;
        }

        @Override
        public Stream<Annotation> getAnnotations()
        {
            return Optional.ofNullable(this.field)
                           .map(f -> Arrays.asList(f.getDeclaredAnnotations())
                                           .stream())
                           .orElse(Stream.empty());
        }

        @SuppressWarnings("unchecked")
        @Override
        public <A extends Annotation> Stream<A> getAnnotations(Class<A> annotationType)
        {
            return this.getAnnotations()
                       .filter(annotation -> annotationType.isAssignableFrom(annotation.getClass()))
                       .map(a -> (A) a);
        }

        @Override
        public String getName()
        {
            return this.field.getName();
        }

        @Override
        public Class<?> getType()
        {
            return this.field.getType();
        }

        @Override
        public java.lang.reflect.Field getRawField()
        {
            return this.field;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T2> FieldReflectionTyped<T2> withType(Class<T2> type)
        {
            return (FieldReflectionTyped<T2>) this;
        }
    }

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
        public <A extends Annotation> Stream<A> getAnnotations(Class<A> annotationType)
        {
            return Stream.of(this.method.getAnnotationsByType(annotationType));
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
                    }
                    catch (Exception e)
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

        public <A extends Annotation> Stream<A> getAnnotations(Class<A> annotationType);
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

    public static interface UntypedField
    {
        public String getName();

        public Class<?> getType();

        public Stream<Annotation> getAnnotations();

        public <A extends Annotation> Stream<A> getAnnotations(Class<A> annotationType);

        public java.lang.reflect.Field getRawField();
    }

    public static interface Field<T> extends UntypedField
    {

    }

    public static interface TypeReflection<T>
    {
        public Stream<Class<?>> getSuperTypes();

        public Stream<Class<?>> getInterfaces();

        public Stream<Class<?>> getSuperTypesAndInterfaces();

        public Stream<Method<T>> getMethods();

        public Optional<Field<T>> getField(String fieldName);
    }

    public static <T> TypeReflection<T> of(Class<T> type)
    {
        return new TypeReflection<T>()
        {
            @Override
            public Stream<Class<?>> getInterfaces()
            {
                return Arrays.asList(type.getInterfaces())
                             .stream()
                             .filter(interfaceType -> interfaceType != null)
                             .flatMap(parentType -> Stream.concat(Stream.of(parentType), ReflectionUtils.of(parentType)
                                                                                                        .getInterfaces()));
            }

            @Override
            public Stream<Class<?>> getSuperTypes()
            {
                return Arrays.asList(type.getSuperclass())
                             .stream()
                             .filter(superType -> superType != null)
                             .flatMap(parentType -> Stream.concat(Stream.of(parentType), ReflectionUtils.of(parentType)
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
                return Stream.concat(declaredMethods.stream(), this.getSuperTypesAndInterfaces()
                                                                   .flatMap(parentType -> Arrays.asList(parentType.getDeclaredMethods())
                                                                                                .stream()))
                             .distinct()
                             .map(MethodImpl::new);
            }

            @Override
            public Optional<Field<T>> getField(String fieldName)
            {
                return Optional.ofNullable(this.determineRawField(type, fieldName))
                               .map(rawField -> ReflectionUtils.of(type, rawField));
            }

            private java.lang.reflect.Field determineRawField(Class<T> type, String fieldName)
            {
                return StreamUtils.fromStreams(() -> Stream.of(type), () -> this.getSuperTypesAndInterfaces())
                                  .map(t ->
                                  {
                                      try
                                      {
                                          return t.getDeclaredField(fieldName);
                                      }
                                      catch (Exception e)
                                      {
                                          return null;
                                      }
                                  })
                                  .filter(field -> field != null)
                                  .findFirst()
                                  .orElse(null);
            }

        };
    }

    public static interface FieldReflection extends UntypedField
    {
        public <T> FieldReflectionTyped<T> withType(Class<T> type);
    }

    public static interface FieldReflectionTyped<T> extends FieldReflection, Field<T>
    {
    }

    public static FieldReflection of(java.lang.reflect.Field field)
    {
        return new FieldReflectionImpl<Object>(field);
    }

    public static <T> FieldReflectionTyped<T> of(Class<T> type, java.lang.reflect.Field field)
    {
        return of(field).withType(type);
    }

    /**
     * Returns a new instance of the given {@link Class} type with the given parameters
     * 
     * @param type
     * @param parameters
     * @return
     */
    public static <T> T newInstance(Class<T> type, Object... parameters)
    {
        T retval = null;
        try
        {
            @SuppressWarnings("rawtypes")
            Class[] parameterTypes = Arrays.asList(parameters)
                                           .stream()
                                           .map(parameter -> parameter.getClass())
                                           .collect(Collectors.toList())
                                           .toArray(new Class[0]);
            @SuppressWarnings("unchecked")
            Constructor<T> constructor = (Constructor<T>) Arrays.asList(type.getConstructors())
                                                                .stream()
                                                                .filter(c -> StreamUtils.merge(Arrays.asList(c.getParameterTypes())
                                                                                                     .stream(),
                                                                                               Arrays.asList(parameterTypes)
                                                                                                     .stream())
                                                                                        .allMatch(lar -> lar.getLeft() != null && lar.getRight() != null
                                                                                                && lar.getLeft()
                                                                                                      .isAssignableFrom(lar.getRight())))
                                                                .findFirst()
                                                                .get();
            constructor.setAccessible(true);
            retval = constructor.newInstance(parameters);
        }
        catch (NoSuchElementException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e)
        {
            throw new IllegalStateException(e);
        }
        return retval;
    }

}
