/*******************************************************************************
 * Copyright 2026 Danny Kunz
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
package org.apache.commons.lang;

import java.util.Objects;

/**
 * Migration shim for the discontinued commons-lang 2.x {@link org.apache.commons.lang.ObjectUtils}.
 *
 * @deprecated use {@link org.apache.commons.lang3.ObjectUtils} instead - except for the two methods commons-lang3 dropped:<br>
 *             <br>
 *             <ul>
 *             <li>{@link #equals(Object, Object)} - removed in commons-lang3 3.5, use {@link Objects#equals(Object, Object)}</li>
 *             <li>{@link #toString(Object)} / {@link #toString(Object, String)} - use
 *             {@link Objects#toString(Object, String)}</li>
 *             </ul>
 *             {@link #defaultIfNull(Object, Object)}, {@link #notEqual(Object, Object)} and
 *             {@link #identityToString(Object)} exist unchanged in commons-lang3.
 * @author omnaest
 */
@Deprecated(forRemoval = true)
public class ObjectUtils
{
    public static Object defaultIfNull(Object object, Object defaultValue)
    {
        return object != null ? object : defaultValue;
    }

    /**
     * Removed in commons-lang3 3.5, so implemented here rather than delegated.
     *
     * @see Objects#equals(Object, Object)
     * @param object1
     * @param object2
     * @return
     */
    public static boolean equals(Object object1, Object object2)
    {
        return Objects.equals(object1, object2);
    }

    public static boolean notEqual(Object object1, Object object2)
    {
        return !Objects.equals(object1, object2);
    }

    public static String identityToString(Object object)
    {
        return org.apache.commons.lang3.ObjectUtils.identityToString(object);
    }

    public static void identityToString(StringBuffer buffer, Object object)
    {
        if (object == null)
        {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        buffer.append(object.getClass()
                            .getName())
              .append('@')
              .append(Integer.toHexString(System.identityHashCode(object)));
    }

    /**
     * Removed in commons-lang3, so implemented here rather than delegated.
     *
     * @see Objects#toString(Object, String)
     * @param object
     * @return
     */
    public static String toString(Object object)
    {
        return object == null ? "" : object.toString();
    }

    public static String toString(Object object, String nullStr)
    {
        return object == null ? nullStr : object.toString();
    }
}
