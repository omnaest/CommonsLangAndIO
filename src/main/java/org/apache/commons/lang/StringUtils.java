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

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

/**
 * Migration shim for the discontinued commons-lang 2.x {@link org.apache.commons.lang.StringUtils}. Delegates to
 * {@link org.apache.commons.lang3.StringUtils} so that the commons-lang 2.6 artifact can be dropped without touching every call site
 * first.
 *
 * @deprecated use {@link org.apache.commons.lang3.StringUtils} instead. Replace the import
 *             <code>org.apache.commons.lang.StringUtils</code> with <code>org.apache.commons.lang3.StringUtils</code>; every method
 *             below exists there under the same name and signature.<br>
 *             <br>
 *             One behaviour difference to check while migrating: {@link #isNumeric(String)} returns <code>true</code> for the empty
 *             string here (commons-lang 2.x semantics, retained deliberately), whereas commons-lang3 returns <code>false</code>.
 * @author omnaest
 */
@Deprecated(forRemoval = true)
public class StringUtils
{
    public static boolean isBlank(String str)
    {
        return org.apache.commons.lang3.StringUtils.isBlank(str);
    }

    public static boolean isNotBlank(String str)
    {
        return org.apache.commons.lang3.StringUtils.isNotBlank(str);
    }

    public static String trim(String str)
    {
        return org.apache.commons.lang3.StringUtils.trim(str);
    }

    public static boolean equals(String str1, String str2)
    {
        return org.apache.commons.lang3.StringUtils.equals(str1, str2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2)
    {
        return org.apache.commons.lang3.StringUtils.equalsIgnoreCase(str1, str2);
    }

    public static int indexOf(String str, char searchChar)
    {
        return org.apache.commons.lang3.StringUtils.indexOf(str, searchChar);
    }

    public static int indexOf(String str, char searchChar, int startPos)
    {
        return org.apache.commons.lang3.StringUtils.indexOf(str, searchChar, startPos);
    }

    public static int indexOf(String str, String searchStr)
    {
        return org.apache.commons.lang3.StringUtils.indexOf(str, searchStr);
    }

    public static int indexOf(String str, String searchStr, int startPos)
    {
        return org.apache.commons.lang3.StringUtils.indexOf(str, searchStr, startPos);
    }

    public static boolean containsIgnoreCase(String str, String searchStr)
    {
        return org.apache.commons.lang3.StringUtils.containsIgnoreCase(str, searchStr);
    }

    public static String substring(String str, int start)
    {
        return org.apache.commons.lang3.StringUtils.substring(str, start);
    }

    public static String substring(String str, int start, int end)
    {
        return org.apache.commons.lang3.StringUtils.substring(str, start, end);
    }

    public static String[] split(String str)
    {
        return org.apache.commons.lang3.StringUtils.split(str);
    }

    public static String[] split(String str, char separatorChar)
    {
        return org.apache.commons.lang3.StringUtils.split(str, separatorChar);
    }

    public static String[] split(String str, String separatorChars)
    {
        return org.apache.commons.lang3.StringUtils.split(str, separatorChars);
    }

    public static String[] split(String str, String separatorChars, int max)
    {
        return org.apache.commons.lang3.StringUtils.split(str, separatorChars, max);
    }

    public static String[] splitPreserveAllTokens(String str)
    {
        return org.apache.commons.lang3.StringUtils.splitPreserveAllTokens(str);
    }

    public static String[] splitPreserveAllTokens(String str, char separatorChar)
    {
        return org.apache.commons.lang3.StringUtils.splitPreserveAllTokens(str, separatorChar);
    }

    public static String[] splitPreserveAllTokens(String str, String separatorChars)
    {
        return org.apache.commons.lang3.StringUtils.splitPreserveAllTokens(str, separatorChars);
    }

    public static String[] splitPreserveAllTokens(String str, String separatorChars, int max)
    {
        return org.apache.commons.lang3.StringUtils.splitPreserveAllTokens(str, separatorChars, max);
    }

    public static String join(Object[] array)
    {
        return org.apache.commons.lang3.StringUtils.join(array);
    }

    public static String join(Object[] array, char separator)
    {
        return org.apache.commons.lang3.StringUtils.join(array, separator);
    }

    public static String join(Object[] array, char separator, int startIndex, int endIndex)
    {
        return org.apache.commons.lang3.StringUtils.join(array, separator, startIndex, endIndex);
    }

    public static String join(Object[] array, String separator)
    {
        return org.apache.commons.lang3.StringUtils.join(array, separator);
    }

    public static String join(Object[] array, String separator, int startIndex, int endIndex)
    {
        return org.apache.commons.lang3.StringUtils.join(array, separator, startIndex, endIndex);
    }

    public static String join(Iterator<?> iterator, char separator)
    {
        return org.apache.commons.lang3.StringUtils.join(iterator, separator);
    }

    public static String join(Iterator<?> iterator, String separator)
    {
        return org.apache.commons.lang3.StringUtils.join(iterator, separator);
    }

    public static String join(Collection<?> collection, char separator)
    {
        return org.apache.commons.lang3.StringUtils.join(collection, separator);
    }

    public static String join(Collection<?> collection, String separator)
    {
        return org.apache.commons.lang3.StringUtils.join(collection, separator);
    }

    public static String removeStart(String str, String remove)
    {
        return org.apache.commons.lang3.StringUtils.removeStart(str, remove);
    }

    public static String removeEnd(String str, String remove)
    {
        return org.apache.commons.lang3.StringUtils.removeEnd(str, remove);
    }

    public static String remove(String str, String remove)
    {
        return org.apache.commons.lang3.StringUtils.remove(str, remove);
    }

    public static String remove(String str, char remove)
    {
        return org.apache.commons.lang3.StringUtils.remove(str, remove);
    }

    public static String replace(String text, String searchString, String replacement)
    {
        return org.apache.commons.lang3.StringUtils.replace(text, searchString, replacement);
    }

    public static String replace(String text, String searchString, String replacement, int max)
    {
        return org.apache.commons.lang3.StringUtils.replace(text, searchString, replacement, max);
    }

    public static String upperCase(String str)
    {
        return org.apache.commons.lang3.StringUtils.upperCase(str);
    }

    public static String upperCase(String str, Locale locale)
    {
        return org.apache.commons.lang3.StringUtils.upperCase(str, locale);
    }

    public static String lowerCase(String str)
    {
        return org.apache.commons.lang3.StringUtils.lowerCase(str);
    }

    public static String lowerCase(String str, Locale locale)
    {
        return org.apache.commons.lang3.StringUtils.lowerCase(str, locale);
    }

    public static String uncapitalize(String str)
    {
        return org.apache.commons.lang3.StringUtils.uncapitalize(str);
    }

    /**
     * Retains the commons-lang 2.x contract: an empty {@link String} counts as numeric. commons-lang3 returns <code>false</code> for
     * the empty {@link String}, so this method is implemented here rather than delegated.
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str)
    {
        if (str == null)
        {
            return false;
        }
        int length = str.length();
        for (int ii = 0; ii < length; ii++)
        {
            if (!Character.isDigit(str.charAt(ii)))
            {
                return false;
            }
        }
        return true;
    }

    public static String defaultString(String str)
    {
        return str == null ? "" : str;
    }

    public static String defaultString(String str, String defaultStr)
    {
        return str == null ? defaultStr : str;
    }

    public static boolean startsWith(String str, String prefix)
    {
        return org.apache.commons.lang3.StringUtils.startsWith(str, prefix);
    }

    public static boolean startsWithAny(String string, String[] searchStrings)
    {
        return org.apache.commons.lang3.StringUtils.startsWithAny(string, searchStrings);
    }
}
