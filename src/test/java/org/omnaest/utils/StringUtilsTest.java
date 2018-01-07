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

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

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

}
