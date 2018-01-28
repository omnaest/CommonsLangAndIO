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

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Test;
import org.omnaest.utils.MatcherUtils.Match;

/**
 * @see MatcherUtils
 * @author omnaest
 */
public class MatcherUtilsTest
{

    @Test
    public void testFind() throws Exception
    {
        Match match = MatcherUtils.matcher()
                                  .of(Pattern.compile("(bcd|other)"))
                                  .findIn("abcde")
                                  .orElse(Stream.empty())
                                  .findFirst()
                                  .get();
        assertEquals(1, match.getStart());
        assertEquals(3, match.getEnd());
        assertEquals("bcd", match.getMatchRegion());
        assertEquals("a   e", match.replaceWith("   "));
        assertEquals("bcd", match.getGroups()
                                 .get(1));

    }

    @Test
    public void testMatch() throws Exception
    {
        Match match = MatcherUtils.matcher()
                                  .of(Pattern.compile("a(bcd|other)e"))
                                  .matchAgainst("abcde")
                                  .get();
        assertEquals(0, match.getStart());
        assertEquals(4, match.getEnd());
        assertEquals("else", match.replaceWith("else"));
        assertEquals("bcd", match.getGroups()
                                 .get(1));

    }

    @Test
    public void testGroupReplacement()
    {
        Match match = MatcherUtils.matcher()
                                  .of(Pattern.compile("ab(cd)ef"))
                                  .matchAgainst("abcdef")
                                  .get();

        String result = match.replaceGroupsWith("xy");
        assertEquals("abxyef", result);
    }

    @Test
    public void testGroupReplacement2()
    {
        Pattern pattern = Pattern.compile("rotate\\([0-9 ]+\\,([0-9 ]+)\\,([0-9 ]+)\\)");
        Match match = MatcherUtils.matcher()
                                  .of(pattern)
                                  .matchAgainst("rotate(90, 10  , 15)")
                                  .get();

        String result = match.replaceGroupsWith("20", "30");
        assertEquals("rotate(90,20,30)", result);
    }

}
