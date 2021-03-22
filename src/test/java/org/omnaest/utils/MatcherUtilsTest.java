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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

    @Test
    public void testExactMatch()
    {
        assertEquals("_cacbcdef_gh", MatcherUtils.matcher()
                                                 .ofExact("ab")
                                                 .findInAnd("abcacbcdefabgh")
                                                 .replace(token -> "_"));
        assertEquals("abca_c*bcdefa?*bgh", MatcherUtils.matcher()
                                                       .ofExact(".*")
                                                       .findInAnd("abca.*c*bcdefa?*bgh")
                                                       .replace(token -> "_"));
    }

    @Test
    public void testSubRegionReplacement()
    {
        assertEquals("_cd_ef_", MatcherUtils.matcher()
                                            .of(Pattern.compile("ab"))
                                            .findInAnd("abcdabefab")
                                            .replace(token -> "_"));
        assertEquals("s_cd_ef_e", MatcherUtils.matcher()
                                              .of(Pattern.compile("ab"))
                                              .findInAnd("sabcdabefabe")
                                              .replace(token -> "_"));
        assertEquals("", MatcherUtils.matcher()
                                     .of(Pattern.compile("ab"))
                                     .findInAnd("")
                                     .replace(token -> "_"));
        assertEquals("def", MatcherUtils.matcher()
                                        .ofExact("ab")
                                        .findInAnd("def")
                                        .replace(token -> "_"));
        assertEquals("!§$%$/&/& _ (/()(=)=?`,.-*", MatcherUtils.matcher()
                                                               .ofExact("ab")
                                                               .findInAnd("!§$%$/&/& ab (/()(=)=?`,.-*")
                                                               .replace(token -> "_"));

    }

    @Test
    public void testReplacer() throws Exception
    {
        assertEquals("bbbc", MatcherUtils.replacer()
                                         .addExactMatchReplacement("a", "b")
                                         .findAndReplaceAllIn("abac"));
        assertEquals("bbbbc", MatcherUtils.replacer()
                                          .addRegExMatchReplacement("[ad]", "b")
                                          .findAndReplaceAllIn("abdac"));
    }

    @Test
    public void testMatcherBuilder() throws Exception
    {
        assertEquals(Arrays.asList("abc", "def")
                           .stream()
                           .collect(Collectors.toSet()),
                     MatcherUtils.matcherBuilder()
                                 .ofAnyExact("abc", "def")
                                 .build()
                                 .findInAnd("abcxxxxzzzzdeftttt")
                                 .getMatchedTokens());
        assertEquals(Arrays.asList("abcs", "def", "ghies")
                           .stream()
                           .collect(Collectors.toSet()),
                     MatcherUtils.matcherBuilder()
                                 .ofAnyExact("abc", "def", "ghi")
                                 .withExactOptionalSuffixes("s", "es")
                                 .build()
                                 .findInAnd("abcsxxghiesxxzzzzdeftttt")
                                 .getMatchedTokens());
    }

}
