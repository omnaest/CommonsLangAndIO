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
 * Migration shim for the discontinued commons-lang 2.x {@link org.apache.commons.lang.BooleanUtils}.
 *
 * @deprecated use {@link org.apache.commons.lang3.BooleanUtils} instead. Replace the import
 *             <code>org.apache.commons.lang.BooleanUtils</code> with <code>org.apache.commons.lang3.BooleanUtils</code>; every
 *             method below exists there under the same name and signature.
 * @author omnaest
 */
@Deprecated(forRemoval = true)
public class BooleanUtils
{
    public static boolean isTrue(Boolean bool)
    {
        return org.apache.commons.lang3.BooleanUtils.isTrue(bool);
    }

    public static boolean toBoolean(Boolean bool)
    {
        return org.apache.commons.lang3.BooleanUtils.toBoolean(bool);
    }

    public static boolean toBoolean(int value)
    {
        return org.apache.commons.lang3.BooleanUtils.toBoolean(value);
    }

    public static boolean toBoolean(int value, int trueValue, int falseValue)
    {
        return org.apache.commons.lang3.BooleanUtils.toBoolean(value, trueValue, falseValue);
    }

    public static boolean toBoolean(Integer value, Integer trueValue, Integer falseValue)
    {
        return org.apache.commons.lang3.BooleanUtils.toBoolean(value, trueValue, falseValue);
    }

    public static boolean toBoolean(String str)
    {
        return org.apache.commons.lang3.BooleanUtils.toBoolean(str);
    }

    public static boolean toBoolean(String str, String trueString, String falseString)
    {
        return org.apache.commons.lang3.BooleanUtils.toBoolean(str, trueString, falseString);
    }
}
