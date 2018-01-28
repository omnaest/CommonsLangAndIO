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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.element.cached.CachedElement;

/**
 * Helper for {@link MatchFinderBuilder} operations
 * 
 * @author omnaest
 */
public class MatcherUtils
{
    /**
     * @see #of(Pattern)
     * @author omnaest
     */
    public static interface MatchFinderBuilder
    {
        public MatchFinder of(Pattern pattern);
    }

    /**
     * @see #findIn(String)
     * @see #matchAgainst(String)
     * @author omnaest
     */
    public static interface MatchFinder
    {
        /**
         * Returns an {@link Optional} of {@link Match} elements representing all matching regions found in the given input
         * 
         * @param input
         * @return
         */
        public Optional<Stream<Match>> findIn(String input);

        public Optional<Match> matchAgainst(String input);
    }

    /**
     * @see #getStart()
     * @see #getEnd()
     * @see #getGroups()
     * @author omnaest
     */
    public static interface Match
    {
        /**
         * Returns the index of the first matched character
         * 
         * @return
         */
        public int getStart();

        /**
         * Returns the index of the last matched character
         * 
         * @return
         */
        public int getEnd();

        /**
         * Returns a {@link String} where the matched area is replaced by the given replacement
         * 
         * @param replacement
         * @return
         */
        public String replaceWith(String replacement);

        /**
         * Returns a {@link RegionReplacer} for the {@link Match#getStart()} and {@link Match#getEnd()} region
         * 
         * @param replacements
         * @return
         */
        public RegionReplacer asReplacer(Supplier<String> replacements);

        /**
         * Returns a {@link Map} containing all the match groups. <br>
         * <br>
         * 0 = whole match group
         * 1,2,... specific match groups
         * 
         * @return
         */
        public Map<Integer, String> getGroups();

        /**
         * Returns the input region between {@link #getStart()} and {@link #getEnd()} inclusive
         * 
         * @return
         */
        public String getMatchRegion();

        /**
         * Returns the tokens of the matched subgroups
         * 
         * @return
         */
        public Stream<String> getSubGroupsAsStream();

        /**
         * Replaces the groups with the given replacements in the given order. If any replacement is null, the group at that position is unchanged.
         * 
         * @param groups
         * @return
         */
        public String replaceGroupsWith(String... replacements);

        /**
         * Replaces the groups with the given replacements. The keys of the given {@link Map} indicates the group index. The group index = 1,2,3,...
         * 
         * @see #getGroups()
         * @param groups
         * @return
         */
        public String replaceGroupsWith(Map<Integer, String> replacements);

    }

    /**
     * Replaces a start to end region for a given {@link String} by a replacement
     * 
     * @author omnaest
     */
    public static interface RegionReplacer extends Function<String, String>
    {
    }

    public static MatchFinderBuilder matcher()
    {
        return new MatchFinderBuilder()
        {
            @Override
            public MatchFinder of(Pattern pattern)
            {
                return new MatchFinder()
                {
                    @Override
                    public Optional<Match> matchAgainst(String input)
                    {
                        Matcher matcher = pattern.matcher(input);
                        Supplier<Boolean> matcherAction = () -> matcher.matches();
                        return this.determineMatches(input, matcher, matcherAction)
                                   .orElseGet(() -> Stream.empty())
                                   .findFirst();
                    }

                    @Override
                    public Optional<Stream<Match>> findIn(String input)
                    {
                        Matcher matcher = pattern.matcher(input);
                        Supplier<Boolean> matcherAction = () -> matcher.find();
                        return this.determineMatches(input, matcher, matcherAction);
                    }

                    private Optional<Stream<Match>> determineMatches(String input, Matcher matcher, Supplier<Boolean> matcherAction)
                    {
                        Iterator<Match> iterator = new Iterator<Match>()
                        {
                            private CachedElement<Match> matchCache = CachedElement.of(() -> this.determineNextMatch());

                            @Override
                            public boolean hasNext()
                            {
                                return this.matchCache.get() != null;
                            }

                            private Match determineNextMatch()
                            {
                                Match retval = null;

                                if (matcherAction.get())
                                {
                                    int start = matcher.start();
                                    int end = matcher.end();

                                    Map<Integer, String> groups = new LinkedHashMap<>();
                                    Map<Integer, BiElement<Integer, Integer>> groupToRegion = new LinkedHashMap<>();
                                    for (int ii = 0; ii <= matcher.groupCount(); ii++)
                                    {
                                        groups.put(ii, matcher.group(ii));
                                        groupToRegion.put(ii, BiElement.of(matcher.start(ii), matcher.end(ii)));

                                    }

                                    retval = new Match()
                                    {
                                        @Override
                                        public int getStart()
                                        {
                                            return start;
                                        }

                                        @Override
                                        public int getEnd()
                                        {
                                            return end - 1;
                                        }

                                        @Override
                                        public String getMatchRegion()
                                        {
                                            return input.substring(start, end);
                                        }

                                        @Override
                                        public Map<Integer, String> getGroups()
                                        {
                                            return groups;
                                        }

                                        @Override
                                        public Stream<String> getSubGroupsAsStream()
                                        {
                                            return groups.values()
                                                         .stream()
                                                         .skip(1);
                                        }

                                        @Override
                                        public String replaceGroupsWith(String... replacements)
                                        {
                                            Map<Integer, String> map = new HashMap<>(this.getGroups());
                                            map.remove(0);
                                            if (replacements != null)
                                            {
                                                for (int ii = 0; ii < replacements.length; ii++)
                                                {
                                                    String replacement = replacements[ii];
                                                    if (replacement != null)
                                                    {
                                                        map.put(ii + 1, replacement);
                                                    }
                                                }
                                            }
                                            return this.replaceGroupsWith(map);
                                        }

                                        @Override
                                        public String replaceGroupsWith(Map<Integer, String> replacements)
                                        {
                                            StringBuilder sb = new StringBuilder(this.getMatchRegion());

                                            replacements.entrySet()
                                                        .stream()
                                                        .sorted(ComparatorUtils.builder()
                                                                               .of(Map.Entry.class)
                                                                               .with(entry -> entry.getKey())
                                                                               .reverse()
                                                                               .build())
                                                        .forEach(entry ->
                                                        {
                                                            Integer group = entry.getKey();
                                                            String replacement = entry.getValue();

                                                            BiElement<Integer, Integer> startAndEnd = groupToRegion.get(group);
                                                            if (startAndEnd != null)
                                                            {
                                                                sb.replace(startAndEnd.getFirst(), startAndEnd.getSecond(), replacement);
                                                            }
                                                        });

                                            return sb.toString();
                                        }

                                        @Override
                                        public String replaceWith(String replacement)
                                        {
                                            return this.asReplacer(() -> replacement)
                                                       .apply(input);
                                        }

                                        @Override
                                        public RegionReplacer asReplacer(Supplier<String> replacements)
                                        {
                                            return input -> StringUtils.builder()
                                                                       .append(input.substring(0, start))
                                                                       .append(replacements.get())
                                                                       .append(input.substring(end))
                                                                       .toString();
                                        }

                                    };
                                }

                                return retval;
                            }

                            @Override
                            public Match next()
                            {
                                return this.matchCache.getAndReset();
                            }

                        };

                        Stream<Match> matches = StreamUtils.fromIterator(iterator);
                        return Optional.of(matches);
                    }
                };
            }

        };
    }
}
