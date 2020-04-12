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
import java.util.function.Function;
import java.util.function.Supplier;

import org.omnaest.utils.ExceptionUtils.Operation;
import org.omnaest.utils.ExceptionUtils.RuntimeExceptionHandler;

/**
 * Helper for common {@link Object} instances
 * 
 * @author omnaest
 */
public class ObjectUtils
{
    private static final List<Class<?>> primitiveTypes = Arrays.asList(Long.class, Integer.class, Short.class, Byte.class, Double.class, Float.class,
                                                                       Character.class, Boolean.class);

    /**
     * Returns true if the given type is a primitive or {@link String}
     * 
     * @param type
     * @return
     */
    public static boolean isPrimitiveOrString(Class<?> type)
    {
        return type != null && (isPrimitiveType(type) || String.class.isAssignableFrom(type));
    }

    /**
     * Returns true if the given type is a primitive
     * 
     * @param type
     * @return
     */
    private static boolean isPrimitiveType(Class<?> type)
    {
        return type != null && (type.isPrimitive() || primitiveTypes.stream()
                                                                    .anyMatch(itype -> itype.isAssignableFrom(type)));
    }

    /**
     * Returns the default object if the given primary object is null
     * 
     * @see #defaultIfNull(Object, Supplier)
     * @param object
     * @param defaultObject
     * @return
     */
    public static <E> E defaultIfNull(E object, E defaultObject)
    {
        return object != null ? object : defaultObject;
    }

    /**
     * Returns the object if it is not null or the {@link Supplier#get()} object
     * 
     * @param object
     * @param defaultObjectSupplier
     * @return
     */
    public static <E> E defaultIfNull(E object, Supplier<E> defaultObjectSupplier)
    {
        return object != null ? object : defaultObjectSupplier.get();
    }

    /**
     * Returns the element from the {@link Supplier#get()} if the test object is not null, otherwise null is returned
     * 
     * @param testObject
     * @param supplier
     * @return
     */
    public static <E, T> E getIfNotNull(T testObject, Supplier<E> supplier)
    {
        return testObject != null ? supplier.get() : null;
    }

    /**
     * Similar to {@link #getOrDefaultIfNotNull(Object, Function, Function)}
     * 
     * @param testObject
     * @param supplier
     * @param defaultSupplier
     * @return
     */
    public static <E, T> E getOrDefaultIfNotNull(T testObject, Supplier<E> supplier, Supplier<E> defaultSupplier)
    {
        return testObject != null ? supplier.get() : defaultSupplier.get();
    }

    /**
     * Similar to {@link #getIfNotNull(Object, Supplier)} but takes a {@link Function} as argument which gets the test object supplier
     * 
     * @param testObject
     * @param supplier
     * @return
     */
    public static <E, T> E getIfNotNull(T testObject, Function<T, E> supplier)
    {
        return testObject != null ? supplier.apply(testObject) : null;
    }

    /**
     * Forwards the test {@link Object} to the {@link Consumer} if it is not null
     * 
     * @param testObject
     * @param consumer
     */
    public static <T> void ifNotNull(T testObject, Consumer<T> consumer)
    {
        if (testObject != null)
        {
            consumer.accept(testObject);
        }
    }

    /**
     * If the given test object is not null the supplier {@link Function} is called otherwise the default supplier {@link Function}
     * 
     * @param testObject
     * @param supplier
     * @param defaultSupplier
     * @return
     */
    public static <E, T> E getOrDefaultIfNotNull(T testObject, Function<T, E> supplier, Function<T, E> defaultSupplier)
    {
        return testObject != null ? supplier.apply(testObject) : defaultSupplier.apply(testObject);
    }

    /**
     * Returns the {@link Supplier#get()} element and catches any {@link Exception} thrown by the {@link Supplier}
     * 
     * @see #getCatchingException(Supplier, Consumer)
     * @see ExceptionUtils#executeSilent(Operation, RuntimeExceptionHandler)
     * @param supplier
     * @return {@link Supplier#get()} or null in the case of any {@link Exception}
     */
    public static <E> E getCatchingException(Supplier<E> supplier)
    {
        Consumer<Exception> exceptionHandler = null;
        return getCatchingException(supplier, exceptionHandler);
    }

    /**
     * Returns the {@link Supplier#get()} element and catches any occuring {@link Exception} and providing it to the given {@link Consumer}
     * 
     * @see #getCatchingException(Supplier)
     * @param supplier
     * @param exceptionHandler
     * @return
     */
    public static <E> E getCatchingException(Supplier<E> supplier, Consumer<Exception> exceptionHandler)
    {
        E retval = null;

        try
        {
            retval = supplier.get();
        }
        catch (Exception e)
        {
            if (exceptionHandler != null)
            {
                exceptionHandler.accept(e);
            }
        }

        return retval;
    }
}
