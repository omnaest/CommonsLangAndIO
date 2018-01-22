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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
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
     * Splits a {@link String} into line tokens
     * 
     * @param str
     * @return
     */
    public static Stream<String> splitToStreamByLineSeparator(String str)
    {
        return splitToStreamByRegEx(str, "[\n\r]+");
    }

    /**
     * Returns token frames with a fixed frame size from the original text. E.g. "abcdefghi" -> "abc","def","ghi" for a frame size of 3.
     * 
     * @see #splitToStream(String)
     * @see StreamUtils#framed(int, Stream)
     * @param frameSize
     * @param text
     * @return
     */
    public static Stream<String> splitToframedStream(int frameSize, String text)
    {
        return StreamUtils.framed(frameSize, splitToStream(text))
                          .map(frame -> org.apache.commons.lang3.StringUtils.join(frame));
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
                           .orElse(Stream.empty())
                           .map(match -> match.getMatchRegion());
    }

    /**
     * Translates the given {@link InputStream} to a {@link String} using the given {@link Charset}
     * 
     * @see StandardCharsets
     * @see IOUtils#toString(InputStream, Charset)
     * @param inputStream
     * @param charset
     * @return
     * @throws IOException
     */
    public static String toString(InputStream inputStream, Charset charset) throws IOException
    {
        return IOUtils.toString(inputStream, charset);
    }

    public static Stream<String> splitToStreamByMaxLength(String str, int maxLength)
    {
        return splitToStreamByRegExFind(str, ".{0," + maxLength + "}").filter(token -> !org.apache.commons.lang3.StringUtils.isEmpty(token));
    }

    /**
     * Returns the ngrams of the given {@link String} with the given size
     * 
     * @param str
     * @param size
     * @return
     */
    public static Stream<String> splitToNGramsStream(String str, int size)
    {
        return splitToNGramsStream(splitToStream(str), size);
    }

    /**
     * Returns the ngrams of the given {@link String} {@link Stream} tokens
     * 
     * @see #splitToNGramsStream(String, int)
     * @param stream
     * @param size
     * @return
     */
    public static Stream<String> splitToNGramsStream(Stream<String> stream, int size)
    {
        return windowed(stream, size / 2, size / 2).filter(token -> token.length() == size);
    }

    /**
     * Returns a window around the given {@link String} {@link Stream} tokens containing the previous tokens and tokens coming after.
     * 
     * @param stream
     * @param before
     *            number of tokens before the current element
     * @param after
     *            number of tokens after the current element
     * @return
     */
    public static Stream<String> windowed(Stream<String> stream, int before, int after)
    {
        return StreamUtils.windowed(stream, before, after)
                          .map(window -> window.getAll()
                                               .stream()
                                               .collect(Collectors.joining()));
    }

    public static Stream<Stream<String>> routeByMatch(Stream<String> tokens, String regEx)
    {
        return StreamUtils.routeByMatch(tokens, token -> token.matches(regEx));
    }
}
