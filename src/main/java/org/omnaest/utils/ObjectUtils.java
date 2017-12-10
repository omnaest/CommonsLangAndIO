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

/**
 * Helper for common {@link Object} instances
 * 
 * @author omnaest
 */
public class ObjectUtils
{
	private static final List<Class<?>> primitiveTypes = Arrays.asList(	Long.class, Integer.class, Short.class, Byte.class, Double.class, Float.class,
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
}
