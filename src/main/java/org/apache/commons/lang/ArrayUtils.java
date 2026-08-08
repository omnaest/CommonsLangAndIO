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

/**
 * Migration shim for the discontinued commons-lang 2.x {@link org.apache.commons.lang.ArrayUtils}.
 *
 * @deprecated use {@link org.apache.commons.lang3.ArrayUtils} instead. Replace the import
 *             <code>org.apache.commons.lang.ArrayUtils</code> with <code>org.apache.commons.lang3.ArrayUtils</code>; every method
 *             below exists there under the same name and signature.
 * @author omnaest
 */
@Deprecated(forRemoval = true)
public class ArrayUtils
{
    public static Object[] clone(Object[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static long[] clone(long[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static int[] clone(int[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static short[] clone(short[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static char[] clone(char[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static byte[] clone(byte[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static double[] clone(double[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static float[] clone(float[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static boolean[] clone(boolean[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.clone(array);
    }

    public static char[] toPrimitive(Character[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array);
    }

    public static char[] toPrimitive(Character[] array, char valueForNull)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array, valueForNull);
    }

    public static Character[] toObject(char[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toObject(array);
    }

    public static long[] toPrimitive(Long[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array);
    }

    public static long[] toPrimitive(Long[] array, long valueForNull)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array, valueForNull);
    }

    public static Long[] toObject(long[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toObject(array);
    }

    public static int[] toPrimitive(Integer[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array);
    }

    public static int[] toPrimitive(Integer[] array, int valueForNull)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array, valueForNull);
    }

    public static Integer[] toObject(int[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toObject(array);
    }

    public static short[] toPrimitive(Short[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array);
    }

    public static short[] toPrimitive(Short[] array, short valueForNull)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array, valueForNull);
    }

    public static Short[] toObject(short[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toObject(array);
    }

    public static byte[] toPrimitive(Byte[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array);
    }

    public static byte[] toPrimitive(Byte[] array, byte valueForNull)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array, valueForNull);
    }

    public static Byte[] toObject(byte[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toObject(array);
    }

    public static double[] toPrimitive(Double[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array);
    }

    public static double[] toPrimitive(Double[] array, double valueForNull)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array, valueForNull);
    }

    public static Double[] toObject(double[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toObject(array);
    }

    public static float[] toPrimitive(Float[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array);
    }

    public static float[] toPrimitive(Float[] array, float valueForNull)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array, valueForNull);
    }

    public static Float[] toObject(float[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toObject(array);
    }

    public static boolean[] toPrimitive(Boolean[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array);
    }

    public static boolean[] toPrimitive(Boolean[] array, boolean valueForNull)
    {
        return org.apache.commons.lang3.ArrayUtils.toPrimitive(array, valueForNull);
    }

    public static Boolean[] toObject(boolean[] array)
    {
        return org.apache.commons.lang3.ArrayUtils.toObject(array);
    }
}
