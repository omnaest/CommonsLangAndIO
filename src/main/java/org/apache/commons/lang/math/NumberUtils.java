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
package org.apache.commons.lang.math;

/**
 * Migration shim for the discontinued commons-lang 2.x {@link org.apache.commons.lang.math.NumberUtils}.
 *
 * @deprecated use {@link org.apache.commons.lang3.math.NumberUtils} instead. Replace the import
 *             <code>org.apache.commons.lang.math.NumberUtils</code> with
 *             <code>org.apache.commons.lang3.math.NumberUtils</code>; every method below exists there under the same name and
 *             signature.
 * @author omnaest
 */
@Deprecated(forRemoval = true)
public class NumberUtils
{
    public static int toInt(String str)
    {
        return org.apache.commons.lang3.math.NumberUtils.toInt(str);
    }

    public static int toInt(String str, int defaultValue)
    {
        return org.apache.commons.lang3.math.NumberUtils.toInt(str, defaultValue);
    }

    public static long toLong(String str)
    {
        return org.apache.commons.lang3.math.NumberUtils.toLong(str);
    }

    public static long toLong(String str, long defaultValue)
    {
        return org.apache.commons.lang3.math.NumberUtils.toLong(str, defaultValue);
    }

    public static double toDouble(String str)
    {
        return org.apache.commons.lang3.math.NumberUtils.toDouble(str);
    }

    public static double toDouble(String str, double defaultValue)
    {
        return org.apache.commons.lang3.math.NumberUtils.toDouble(str, defaultValue);
    }
}
