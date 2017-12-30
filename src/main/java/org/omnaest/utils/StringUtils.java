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
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.iterator.StringIterator;

/**
 * Helper for {@link String} operations
 * 
 * @author omnaest
 */
public class StringUtils
{

    /**
     * Returns a new {@link Deque} filled with single characters of the given text
     * 
     * @param text
     * @return
     */
    public static Deque<String> splitToDequeue(String text)
    {
        return new LinkedList<>(splitToStream(text).collect(Collectors.toList()));
    }

    /**
     * Returns a {@link Stream} of single character tokens for the given {@link String}
     * 
     * @see #splitToStream(String, String)
     * @param str
     * @return
     */
    public static Stream<String> splitToStream(String str)
    {
        return StreamUtils.fromIterator(new StringIterator(str));
    }

    /**
     * Returns a {@link Stream} of {@link String} tokens which represents the splitted parts of the given {@link String}
     * 
     * @see #splitToStreamByRegEx(String, String)
     * @see org.apache.commons.lang3.StringUtils#splitPreserveAllTokens(String, String)
     * @param str
     * @param separatorChars
     * @return
     */
    public static Stream<String> splitToStream(String str, String separatorChars)
    {
        String[] tokens = org.apache.commons.lang3.StringUtils.splitPreserveAllTokens(str, separatorChars);
        return tokens != null ? Arrays.asList(tokens)
                                      .stream()
                : Stream.empty();
    }

    /**
     * Similar to {@link #splitToStream(String, String)} using a regex pattern
     * 
     * @see #splitToStream(String, String)
     * @see Pattern
     * @param str
     * @param regex
     * @return
     */
    public static Stream<String> splitToStreamByRegEx(String str, String regex)
    {
        String[] tokens = str != null ? str.split(regex) : null;
        return tokens != null ? Arrays.asList(tokens)
                                      .stream()
                : Stream.empty();
    }

    /**
     * Returns token frames with a fixed frame size from the original text. E.g. "abcdefghi" -> "abc","def","ghi" for a frame size of 3.
     * 
     * @param frameSize
     * @param text
     * @return
     */
    public static Stream<String> frameToStream(int frameSize, String text)
    {
        return splitToStream(text).map(frame -> org.apache.commons.lang3.StringUtils.join(frame));
    }

    /**
     * @see org.apache.commons.lang3.StringUtils#defaultString(String, String)
     * @param str
     * @param defaultStr
     * @return
     */
    public static String defaultIfNull(String str, String defaultStr)
    {
        return org.apache.commons.lang3.StringUtils.defaultString(str, defaultStr);
    }

    /**
     * Reverses the given {@link String}
     * 
     * @param str
     * @return
     */
    public static String reverse(String str)
    {
        return StreamUtils.reverse(splitToStream(str))
                          .collect(Collectors.joining());
    }

    /**
     * Returns a simple {@link String} {@link Comparator}
     * 
     * @return
     */
    public static Comparator<String> comparator()
    {
        return (o1, o2) -> org.apache.commons.lang3.StringUtils.compare(o1, o2);
    }

    /**
     * Returns a {@link StringBuilder}
     * 
     * @return
     */
    public static StringBuilder builder()
    {
        return new StringBuilder();
    }

    /**
     * Returns a {@link Stream} of {@link String} tokens representing all found subtokens matching the given regex
     * 
     * @param str
     * @param regex
     * @return
     */
    public static Stream<String> splitToStreamByRegExFind(String str, String regex)
    {
        return MatcherUtils.matcher()
                           .of(Pattern.compile(regex))
                           .findIn(str)
                           .get()
                           .map(match -> match.getMatchRegion());
    }
}
