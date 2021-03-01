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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.utils.EncoderUtils.TextEncoderAndDecoderFactory;
import org.omnaest.utils.StringUtils.StringEncoderAndDecoder;
import org.omnaest.utils.element.bi.BiElement;

public class StringUtilsTest
{

    @Test
    public void testSplitToStream() throws Exception
    {
        List<String> tokens = StringUtils.splitToStream("abc")
                                         .collect(Collectors.toList());
        assertEquals("a", tokens.get(0));
        assertEquals("b", tokens.get(1));
        assertEquals("c", tokens.get(2));
    }

    @Test
    public void testReverse() throws Exception
    {
        assertEquals("321", StringUtils.reverse("123"));
    }

    @Test
    public void testSplitToStreamByRegEx() throws Exception
    {
        List<String> tokens = StringUtils.splitToStreamByRegEx("abcde", "[bd]")
                                         .collect(Collectors.toList());
        assertEquals("a", tokens.get(0));
        assertEquals("c", tokens.get(1));
        assertEquals("e", tokens.get(2));
    }

    @Test
    public void testSplitToStreamByRegExFind() throws Exception
    {
        List<String> tokens = StringUtils.splitToStreamByRegExFind("abcde", "[bd]")
                                         .collect(Collectors.toList());
        assertEquals("b", tokens.get(0));
        assertEquals("d", tokens.get(1));
    }

    @Test
    public void testSplitToStreamByMaxLength() throws Exception
    {
        List<String> tokens = StringUtils.splitToStreamByMaxLength("abcdefghi", 3)
                                         .collect(Collectors.toList());
        assertEquals(3, tokens.size());
        assertEquals("abc", tokens.get(0));
        assertEquals("def", tokens.get(1));
        assertEquals("ghi", tokens.get(2));
    }

    @Test
    public void testSplitToNGramsStream() throws Exception
    {
        List<String> ngrams = StringUtils.splitToNGramsStream("abcdefgh", 3)
                                         .collect(Collectors.toList());
        assertEquals(6, ngrams.size());
        assertEquals("abc", ngrams.get(0));
        assertEquals("bcd", ngrams.get(1));
        assertEquals("cde", ngrams.get(2));
        assertEquals("def", ngrams.get(3));
        assertEquals("efg", ngrams.get(4));
        assertEquals("fgh", ngrams.get(5));
    }

    @Test
    public void testSplitToNGramsEvenStream() throws Exception
    {
        List<String> ngrams = StringUtils.splitToNGramsStream("abcdefgh", 4)
                                         .collect(Collectors.toList());
        assertEquals(5, ngrams.size());
        assertEquals("abcd", ngrams.get(0));
        assertEquals("bcde", ngrams.get(1));
        assertEquals("cdef", ngrams.get(2));
        assertEquals("defg", ngrams.get(3));
        assertEquals("efgh", ngrams.get(4));
    }

    @Test
    public void testRouteByMatch() throws Exception
    {
        List<String> result = StringUtils.routeByMatch(Arrays.asList("a", "b", "c", "a", "c", "d")
                                                             .stream(),
                                                       "a")
                                         .map(tokens -> tokens.collect(Collectors.joining()))
                                         .collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals("abc", result.get(0));
        assertEquals("acd", result.get(1));
    }

    @Test
    public void testSplitToframedStream() throws Exception
    {
        List<String> tokens = StringUtils.splitToframedStream(3, "abcdefghijklm")
                                         .collect(Collectors.toList());
        Iterator<String> iterator = tokens.iterator();
        assertEquals("abc", iterator.next());
        assertEquals("def", iterator.next());
        assertEquals("ghi", iterator.next());
        assertEquals("jkl", iterator.next());
        assertEquals("m", iterator.next());
    }

    @Test
    public void testFromSupplier() throws Exception
    {
        assertEquals("abc", StringUtils.repeat(new Supplier<String>()
        {
            private int    pos  = 0;
            private String text = "abc";

            @Override
            public String get()
            {
                return this.text.substring(this.pos++, this.pos);
            }
        }, 3));
    }

    @Test
    public void testSplitToNGramsPositionStreamStringInt() throws Exception
    {
        List<BiElement<Long, String>> positionAndNgrams = StringUtils.splitToNGramsPositionStream("abcdefghi", 5)
                                                                     .collect(Collectors.toList());
        assertEquals(5, positionAndNgrams.size());
        assertEquals(Arrays.asList("abcde", "bcdef", "cdefg", "defgh", "efghi"), positionAndNgrams.stream()
                                                                                                  .map(pan -> pan.getSecond())
                                                                                                  .collect(Collectors.toList()));
        assertEquals(Arrays.asList(2l, 3l, 4l, 5l, 6l), positionAndNgrams.stream()
                                                                         .map(pan -> pan.getFirst())
                                                                         .collect(Collectors.toList()));
    }

    @Test
    public void testDistinctCount() throws Exception
    {
        Map<String, Integer> distinctCount = StringUtils.distinctCount("aaaabbcccc");
        assertEquals(3, distinctCount.size());
        assertEquals(4, distinctCount.get("a")
                                     .intValue());
        assertEquals(2, distinctCount.get("b")
                                     .intValue());
        assertEquals(4, distinctCount.get("c")
                                     .intValue());
    }

    @Test
    public void testLastFromBack() throws Exception
    {
        assertEquals("ef", StringUtils.lastFromBack("abcdef", 2));
    }

    @Test
    public void testEnsurePrefix() throws Exception
    {
        assertEquals("/abc", StringUtils.ensurePrefix("abc", "/"));
        assertEquals("/abc", StringUtils.ensurePrefix("/abc", "/"));
        assertEquals("/", StringUtils.ensurePrefix(null, "/"));
    }

    @Test
    public void testRemoveStartAndEnd() throws Exception
    {
        assertEquals("bc", StringUtils.removeStartAndEnd("abcd", 1, 1));
        assertEquals(null, StringUtils.removeStartAndEnd(null, 1, 1));
    }

    @Test
    public void testEncoder() throws Exception
    {
        StringEncoderAndDecoder encoderAndDecoder = StringUtils.encoder()
                                                               .with(TextEncoderAndDecoderFactory::forAlphaNumericText);
        String encodedList = encoderAndDecoder.encodeList(Arrays.asList("a", "b"), ",");
        assertEquals(Arrays.asList("a", "b"), encoderAndDecoder.decodeList(encodedList, ","));
    }

    @Test
    public void testReplaceEach() throws Exception
    {
        assertEquals("1 hated city", StringUtils.replaceEach("my lovely town", map -> map.put("lovely", "hated")
                                                                                         .put("town", "city")
                                                                                         .put("my", 1)));
    }

    @Test
    public void testLimitText() throws Exception
    {
        assertEquals("Once upon a time...", StringUtils.limitText("Once upon a time there was a nice little mouse.", 16, "..."));
        assertEquals("Once upon a time there was a nice little mouse.", StringUtils.limitText("Once upon a time there was a nice little mouse.", 160, "..."));
        assertEquals(null, StringUtils.limitText(null, 16, "..."));
    }

    @Test
    public void testLeftUntil() throws Exception
    {
        assertEquals("abc", StringUtils.leftUntil("abcdef", "de"));
        assertEquals("abcdef", StringUtils.leftUntil("abcdef", "xy"));
    }

    @Test
    public void testNotEqualsAnyFilter() throws Exception
    {
        Predicate<String> filter = StringUtils.notEqualsAnyFilter("a", "b");

        assertEquals(true, filter.test("c"));
        assertEquals(false, filter.test("b"));
        assertEquals(false, filter.test("a"));
    }

}
