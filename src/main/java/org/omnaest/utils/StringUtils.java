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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.omnaest.utils.EncoderUtils.EncoderAndDecoderFactory;
import org.omnaest.utils.EncoderUtils.TextEncoderAndDecoder;
import org.omnaest.utils.EncoderUtils.TextEncoderAndDecoderFactory;
import org.omnaest.utils.MapUtils.MapBuilder;
import org.omnaest.utils.element.bi.BiElement;
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
        return splitToStreamByRegEx(str, "((\n\r)|(\\r\\n)|(\n)|(\r))");
    }

    /**
     * Returns token frames with a fixed frame size from the original text. E.g. "abcdefghi" -> "abc","def","ghi" for a frame size of 3.
     * 
     * @see #splitToStream(String)
     * @see StreamUtils#framedPreserveSize(int, Stream)
     * @param frameSize
     * @param text
     * @return
     */
    public static Stream<String> splitToframedStream(int frameSize, String text)
    {
        return StreamUtils.framedPreserveSize(frameSize, splitToStream(text))
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
     * Returns a {@link StringTextBuilder}
     * 
     * @return
     */
    public static StringTextBuilder builder()
    {
        return new StringTextBuilder()
        {
            private StringBuilder builder       = new StringBuilder();
            private String        lineSeparator = System.lineSeparator();

            @Override
            public StringTextBuilder withLineSeparator(String lineSeparator)
            {
                this.lineSeparator = lineSeparator;
                return this;
            }

            @Override
            public StringTextBuilder add(String text)
            {
                this.builder.append(text);
                return this;
            }

            @Override
            public String build()
            {
                return this.builder.toString();
            }

            @Override
            public StringTextBuilder add(long value)
            {
                this.builder.append(value);
                return this;
            }

            @Override
            public StringTextBuilder addLineBreak()
            {
                return this.add(this.lineSeparator);
            }

            @Override
            public StringTextBuilder addLine(String line)
            {
                return this.add(line)
                           .addLineBreak();
            }

            @Override
            public String toString()
            {
                return this.build();
            }

            @Override
            public StringTextBuilder addLines(List<String> lines)
            {
                Optional.ofNullable(lines)
                        .orElse(Collections.emptyList())
                        .forEach(this::addLine);
                return this;
            }

            @Override
            public StringTextBuilder addLines(String... lines)
            {
                return this.addLines(Optional.ofNullable(lines)
                                             .map(Arrays::asList)
                                             .orElse(Collections.emptyList()));
            }
        };
    }

    public static interface StringTextBuilder
    {
        public StringTextBuilder add(String text);

        public StringTextBuilder add(long value);

        public StringTextBuilder addLineBreak();

        public StringTextBuilder withLineSeparator(String lineSeparator);

        public String build();

        /**
         * Adds a text and a line break
         * 
         * @see #add(String)
         * @see #addLineBreak()
         * @param line
         * @return
         */
        public StringTextBuilder addLine(String line);

        /**
         * Similar to {@link #addLine(String)} but applied to all {@link List} items
         * 
         * @param lines
         * @return
         */
        public StringTextBuilder addLines(List<String> lines);

        /**
         * Similar to {@link #addLines(List)}
         * 
         * @param lines
         * @return
         */
        public StringTextBuilder addLines(String... lines);
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

    /**
     * @see IOUtils#toString(Reader)
     * @param reader
     * @return
     * @throws IOException
     */
    public static String toString(Reader reader) throws IOException
    {
        return IOUtils.toString(reader);
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
     * Similar to {@link #splitToNGramsStream(String, int)} but returns a {@link BiElement} with the read position as {@link BiElement#getFirst()}
     * 
     * @param str
     * @param size
     * @return
     */
    public static Stream<BiElement<Long, String>> splitToNGramsPositionStream(String str, int size)
    {
        return splitToNGramsPositionStream(splitToStream(str), size);
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
        return windowed(stream, (size - 1) / 2, size / 2).filter(token -> token.length() == size);
    }

    /**
     * Similar to {@link #splitToNGramsStream(Stream, int)} but return a {@link BiElement} with the read position as first {@link BiElement#getFirst()}
     * 
     * @param stream
     * @param size
     * @return
     */
    public static Stream<BiElement<Long, String>> splitToNGramsPositionStream(Stream<String> stream, int size)
    {
        return windowedAndPositioned(stream, (size - 1) / 2, size / 2).filter(token -> token.getSecond()
                                                                                            .length() == size);
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

    /**
     * Similar to {@link #windowed(Stream, int, int)} but returns a {@link BiElement} containing the read position as first argument
     * 
     * @param stream
     * @param before
     * @param after
     * @return
     */
    public static Stream<BiElement<Long, String>> windowedAndPositioned(Stream<String> stream, int before, int after)
    {
        return StreamUtils.windowed(stream, before, after)
                          .map(window -> BiElement.of(window.getPosition(), window.getAll()
                                                                                  .stream()
                                                                                  .collect(Collectors.joining())));
    }

    public static Stream<Stream<String>> routeByMatch(Stream<String> tokens, String regEx)
    {
        return StreamUtils.routeByMatch(tokens, token -> token.matches(regEx));
    }

    /**
     * Returns a concatenated {@link String} based on repeated calls to {@link Supplier#get()}
     * 
     * @param supplier
     * @param repeat
     *            number of repeats
     * @return
     */
    public static String repeat(Supplier<String> supplier, int repeat)
    {
        StringBuilder sb = new StringBuilder();
        if (supplier != null)
        {
            for (int ii = 0; ii < repeat; ii++)
            {
                String str = supplier.get();
                if (str != null)
                {
                    sb.append(str);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Similar to {@link #repeat(Supplier, int)}
     * 
     * @param str
     * @param repeat
     * @return
     */
    public static String repeat(String str, int repeat)
    {
        return repeat(() -> str, repeat);
    }

    /**
     * Returns a map with distinct characters of the given {@link String} and their counts
     * 
     * @param str
     * @return
     */
    public static Map<String, Integer> distinctCount(String str)
    {
        Map<String, Integer> retmap = new LinkedHashMap<>();
        splitToStream(str).forEach(character ->
        {
            int count = retmap.getOrDefault(character, 0);
            count++;
            retmap.put(character, count);
        });
        return retmap;
    }

    /**
     * Returns the last part of a given {@link String}. If null is given as {@link String} null is returned.
     * 
     * @param str
     * @param lengthFromBack
     * @return
     */
    public static String lastFromBack(String str, int lengthFromBack)
    {
        String retval = null;
        if (str != null)
        {
            retval = org.apache.commons.lang3.StringUtils.substring(str, str.length() - lengthFromBack, str.length());
        }
        return retval;
    }

    /**
     * Ensures that the given prefix exists.
     * 
     * @param str
     * @param prefix
     * @return
     */
    public static String ensurePrefix(String str, String prefix)
    {
        return org.apache.commons.lang3.StringUtils.startsWith(str, prefix) ? str : prefix + org.apache.commons.lang3.StringUtils.defaultString(str);
    }

    public static String toString(Object object)
    {
        return object == null ? null : String.valueOf(object);
    }

    /**
     * Removes the given number of start and end characters from the given {@link String}
     * 
     * @param str
     * @param startOffset
     * @param endOffset
     * @return
     */
    public static String removeStartAndEnd(String str, int startOffset, int endOffset)
    {
        return org.apache.commons.lang3.StringUtils.substring(str, startOffset, str != null ? str.length() - endOffset : 0);
    }

    public static interface StringEncoderAndDecoderInitializer
    {
        public StringEncoderAndDecoder with(Function<TextEncoderAndDecoderFactory, TextEncoderAndDecoder> factoryConsumer);
    }

    public static interface StringEncoderAndDecoder
    {
        public String encodeList(List<String> list, String delimiter);

        public List<String> decodeList(String encodedText, String delimiter);
    }

    public static StringEncoderAndDecoderInitializer decoder()
    {
        return encoder();
    }

    public static StringEncoderAndDecoderInitializer encoder()
    {
        return new StringEncoderAndDecoderInitializer()
        {
            @Override
            public StringEncoderAndDecoder with(Function<TextEncoderAndDecoderFactory, TextEncoderAndDecoder> consumer)
            {
                EncoderAndDecoderFactory factory = EncoderUtils.newInstance();
                TextEncoderAndDecoder encoderAndDecoder = consumer.apply(factory);
                return new StringEncoderAndDecoder()
                {

                    @Override
                    public String encodeList(List<String> list, String delimiter)
                    {
                        return ListUtils.toStream(list)
                                        .map(token -> encoderAndDecoder.encode(token))
                                        .collect(Collectors.joining(delimiter));
                    }

                    @Override
                    public List<String> decodeList(String encodedText, String delimiter)
                    {
                        return splitToStream(encodedText, delimiter).map(token -> encoderAndDecoder.decode(token))
                                                                    .collect(Collectors.toList());
                    }
                };
            }
        };
    }

    public static String replaceEach(String text, Consumer<MapBuilder<String, ?>> replacementsConsumer)
    {
        MapBuilder<String, Object> builder = MapUtils.<String, Object>builder();
        replacementsConsumer.accept(builder);
        return replaceEach(text, builder.build());
    }

    public static String replaceEach(String text, Map<String, ?> replacements)
    {
        String[] searchList = replacements.keySet()
                                          .stream()
                                          .sorted()
                                          .collect(Collectors.toList())
                                          .toArray(new String[0]);
        String[] replacementList = Arrays.asList(searchList)
                                         .stream()
                                         .map(key -> Optional.ofNullable(replacements.get(key))
                                                             .map(String::valueOf)
                                                             .orElse(null))
                                         .collect(Collectors.toList())
                                         .toArray(new String[0]);
        return org.apache.commons.lang3.StringUtils.replaceEach(text, searchList, replacementList);
    }

    /**
     * Limits the given {@link String} to the given size. If the size is exceeded the substring of that size is returned with the given overflows suffix
     * appended.
     * 
     * @param text
     * @param size
     * @param overflowSuffix
     * @return
     */
    public static String limitText(String text, int size, String overflowSuffix)
    {
        if (org.apache.commons.lang3.StringUtils.length(text) > size)
        {
            return org.apache.commons.lang3.StringUtils.substring(text, 0, size) + (overflowSuffix != null ? overflowSuffix : "");
        }
        else
        {
            return text;
        }
    }

    /**
     * Returns the text part until the given match token. The match token is excluded from the result.
     * 
     * @param text
     * @param matchToken
     * @return
     */
    public static String leftUntil(String text, String matchToken)
    {
        return Optional.ofNullable(org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens(text, matchToken))
                       .filter(tokens -> tokens.length >= 1)
                       .map(tokens -> tokens[0])
                       .orElse("");
    }

    /**
     * Returns a {@link Predicate} that matches all values that are NOT equal to any of the given parameter values
     * 
     * @see #equalsAnyFilter(String...)
     * @param matchValue
     * @return
     */
    public static Predicate<String> notEqualsAnyFilter(String... matchValue)
    {
        return equalsAnyFilter(matchValue).negate();
    }

    /**
     * Returns a {@link Predicate} that matches all values that are equal to any of the given parameter values
     * 
     * @see #notEqualsAnyFilter(String...)
     * @param matchValue
     * @return
     */
    public static Predicate<String> equalsAnyFilter(String... matchValue)
    {
        return value -> org.apache.commons.lang3.StringUtils.equalsAny(value, matchValue);
    }

    public static boolean containsAnyIgnoreCase(String text, String... tokens)
    {
        return Optional.ofNullable(tokens)
                       .map(Stream::of)
                       .orElse(Stream.empty())
                       .anyMatch(token -> org.apache.commons.lang3.StringUtils.containsIgnoreCase(text, token));
    }

}
