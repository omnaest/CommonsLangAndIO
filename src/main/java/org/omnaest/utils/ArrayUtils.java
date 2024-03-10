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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Predicate;

public class ArrayUtils
{
    public static <E> E[][] deepClone(E[][] array)
    {
        E[][] retval = clone(array);
        for (int ii = 0; ii < retval.length; ii++)
        {
            retval[ii] = clone(retval[ii]);
        }
        return retval;
    }

    public static <E> E[] clone(E[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static <E> E last(E[] array)
    {
        return array != null && array.length >= 1 ? array[array.length - 1] : null;
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] reverse(E[] array)
    {
        return array == null ? null
                : ListUtils.reverse(Arrays.asList(array))
                           .toArray((E[]) Array.newInstance(array.getClass()
                                                                 .getComponentType(),
                                                            array.length));
    }

    public static byte[] subArrayStartingFromMatching(Predicate<Byte> matcher, byte[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(subArrayStartingFromMatching(matcher, org.apache.commons.lang3.ArrayUtils.toObject(array)));
    }

    public static <E> E[] subArrayStartingFromMatching(Predicate<E> matcher, E[] array)
    {
        if (array == null)
        {
            return null;
        }
        else if (matcher == null)
        {
            return org.apache.commons.lang3.ArrayUtils.subarray(array, 0, 0);
        }
        else
        {
            for (int i = 0; i < array.length; i++)
            {
                if (matcher.test(array[i]))
                {
                    return org.apache.commons.lang3.ArrayUtils.subarray(array, i, array.length);
                }
            }
            return null;
        }
    }

}
