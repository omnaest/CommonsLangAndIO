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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omnaest.utils.MatcherUtils.MatchFinderBuilder;

public class PatternUtils
{
    /**
     * Returns the {@link Matcher#group(int)} as {@link Map}
     * 
     * @param pattern
     * @param text
     * @return
     */
    public static Map<Integer, String> matchToGroups(Pattern pattern, String text)
    {
        Map<Integer, String> retmap = new LinkedHashMap<>();

        Matcher matcher = pattern.matcher(text);
        if (matcher.matches())
        {
            for (int ii = 0; ii <= matcher.groupCount(); ii++)
            {
                retmap.put(ii, matcher.group(ii));
            }
        }

        return retmap;
    }

    /**
     * @see MatcherUtils#matcher()
     * @return
     */
    public static MatchFinderBuilder matcher()
    {
        return MatcherUtils.matcher();
    }
}
