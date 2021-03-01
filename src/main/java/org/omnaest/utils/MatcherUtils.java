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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.functional.Provider;

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
        /**
         * Matches using a given {@link Pattern}
         * 
         * @param pattern
         * @return
         */
        public MatchFinder of(Pattern pattern);

        /**
         * Matches a given regEx like {@link #of(Pattern)}
         * 
         * @see #of(Pattern)
         * @param regEx
         * @return
         */
        public MatchFinder ofRegEx(String regEx);

        /**
         * Matches the exact {@link String}
         * 
         * @param str
         * @return
         */
        public MatchFinder ofExact(String str);

        /**
         * Matches any of the given {@link String}s
         * 
         * @param exactMatchTokens
         * @return
         */
        public MatchFinder ofAnyExact(Collection<String> exactMatchTokens);

        /**
         * Similar to {@link #ofAnyExact(Collection)}
         * 
         * @param exactMatchTokens
         * @return
         */
        public MatchFinder ofAnyExact(String... exactMatchTokens);
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

        /**
         * Returns a {@link MatchResult} for elements found in the input
         * 
         * @param input
         * @return
         */
        public MatchResult findInAnd(String input);

        public Optional<Match> matchAgainst(String input);
    }

    public static interface MatchResult extends Iterable<Match>
    {
        public Stream<Match> stream();

        public String replace(Function<String, String> replacerFunction);

        public Optional<Match> getFirst();

        public boolean hasMatches();

        MatchResult withNoCaching();
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
         * Returns a match group with the given index.<br>
         * <br>
         * 0 = whole match group
         * 1,2,... specific match groups
         * 
         * @param index
         * @return
         */
        public String getGroup(int index);

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
         * Gets a single sub group at the given group index. Subgroups are starting with 1,2,3,...
         * 
         * @param index
         * @return
         */
        public Optional<String> getSubGroup(int index);

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

        public RegionReplacer asSubRegionReplacer(Supplier<String> replacements);

        public String replaceSubRegionWith(String replacement);

        public String replaceSubRegionWith(Function<String, String> replacerFunction);

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
            public MatchFinder ofRegEx(String regEx)
            {
                return this.of(Pattern.compile(regEx));
            }

            @Override
            public MatchFinder ofExact(String str)
            {
                return this.ofRegEx(Pattern.quote(str));
            }

            @Override
            public MatchFinder ofAnyExact(Collection<String> exactMatchTokens)
            {
                return this.ofRegEx(Optional.ofNullable(exactMatchTokens)
                                            .orElse(Collections.emptyList())
                                            .stream()
                                            .map(token -> Pattern.quote(token))
                                            .collect(Collectors.joining("|")));
            }

            @Override
            public MatchFinder ofAnyExact(String... exactMatchTokens)
            {
                return this.ofAnyExact(Arrays.asList(exactMatchTokens));
            }

            @Override
            public MatchFinder of(Pattern pattern)
            {
                return new MatchFinder()
                {
                    @Override
                    public Optional<Match> matchAgainst(String input)
                    {
                        Optional<Match> retval;
                        if (input != null)
                        {
                            Matcher matcher = pattern.matcher(input);
                            Supplier<Boolean> matcherAction = () -> matcher.matches();
                            retval = this.determineMatches(input, matcher, matcherAction)
                                         .orElseGet(() -> Stream.empty())
                                         .findFirst();
                        }
                        else
                        {
                            retval = Optional.empty();
                        }
                        return retval;
                    }

                    @Override
                    public MatchResult findInAnd(String input)
                    {
                        AssertionUtils.assertIsNotNull("Pattern must not be null", pattern);
                        return new MatchResult()
                        {
                            private Supplier<Stream<Match>> matches = this.createMatchesSupplier();

                            @Override
                            public MatchResult withNoCaching()
                            {
                                this.matches = this.createMatchStreamSupplier();
                                return this;
                            }

                            @Override
                            public Iterator<Match> iterator()
                            {
                                return this.stream()
                                           .iterator();
                            }

                            @Override
                            public Stream<Match> stream()
                            {
                                return this.matches.get();
                            }

                            private Supplier<Stream<Match>> createMatchesSupplier()
                            {
                                Supplier<List<Match>> matchesProvider = CachedElement.of(this.createMatchStreamSupplier()
                                                                                             .and(stream -> stream.collect(Collectors.toList())));
                                return () -> matchesProvider.get()
                                                            .stream();
                            }

                            private Provider<Stream<Match>> createMatchStreamSupplier()
                            {
                                return () -> this.generateMatches();
                            }

                            private Stream<Match> generateMatches()
                            {
                                if (input != null)
                                {
                                    Matcher matcher = pattern.matcher(input);
                                    Supplier<Boolean> matcherAction = () -> matcher.find();
                                    return determineMatches(input, matcher, matcherAction).orElse(Stream.empty());
                                }
                                else
                                {
                                    return Stream.empty();
                                }
                            }

                            @Override
                            public Optional<Match> getFirst()
                            {
                                return this.stream()
                                           .findFirst();
                            }

                            @Override
                            public boolean hasMatches()
                            {
                                return this.getFirst()
                                           .isPresent();
                            }

                            @Override
                            public String replace(Function<String, String> replacerFunction)
                            {
                                String result = input;

                                if (input != null)
                                {
                                    Matcher matcher = pattern.matcher(input);
                                    StringBuffer sb = new StringBuffer();
                                    while (matcher.find())
                                    {
                                        String replacement = replacerFunction.apply(input.substring(matcher.start(), matcher.end()));
                                        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                                    }
                                    matcher.appendTail(sb);
                                    result = sb.toString();
                                }

                                return result;
                            }
                        };
                    }

                    @Override
                    public Optional<Stream<Match>> findIn(String input)
                    {
                        AssertionUtils.assertIsNotNull("Pattern must not be null", pattern);
                        Optional<Stream<Match>> retval;
                        if (input != null)
                        {
                            Matcher matcher = pattern.matcher(input);
                            Supplier<Boolean> matcherAction = () -> matcher.find();
                            retval = this.determineMatches(input, matcher, matcherAction);
                        }
                        else
                        {
                            retval = Optional.empty();
                        }
                        return retval;
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
                                    int regionStart = matcher.regionStart();
                                    int regionEnd = matcher.regionEnd();

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
                                        public String getGroup(int index)
                                        {
                                            return this.getGroups()
                                                       .get(index);
                                        }

                                        @Override
                                        public Stream<String> getSubGroupsAsStream()
                                        {
                                            return groups.values()
                                                         .stream()
                                                         .skip(1);
                                        }

                                        @Override
                                        public Optional<String> getSubGroup(int index)
                                        {
                                            return Optional.ofNullable(this.getGroup(index));
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
                                                                               .with(entry -> (Integer) entry.getKey())
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
                                        public String replaceSubRegionWith(String replacement)
                                        {
                                            return this.asSubRegionReplacer(() -> replacement)
                                                       .apply(input);
                                        }

                                        @Override
                                        public String replaceSubRegionWith(Function<String, String> replacerFunction)
                                        {
                                            return this.asSubRegionReplacer(() -> replacerFunction.apply(this.getMatchRegion()))
                                                       .apply(input);
                                        }

                                        @Override
                                        public RegionReplacer asReplacer(Supplier<String> replacements)
                                        {
                                            return input -> StringUtils.builder()
                                                                       .add(input.substring(0, start))
                                                                       .add(replacements.get())
                                                                       .add(input.substring(end))
                                                                       .build();
                                        }

                                        @Override
                                        public RegionReplacer asSubRegionReplacer(Supplier<String> replacements)
                                        {
                                            return input -> StringUtils.builder()
                                                                       .add(input.substring(regionStart, start))
                                                                       .add(replacements.get())
                                                                       .add(input.substring(regionEnd))
                                                                       .build();
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

    public static Replacer replacer()
    {
        return new Replacer()
        {
            private Map<String, String> exactMatchTokenToValue = new LinkedHashMap<>();
            private Map<String, String> regexToReplacement     = new LinkedHashMap<>();

            @Override
            public Replacer addExactMatchReplacement(String matchToken, String value)
            {
                this.exactMatchTokenToValue.put(matchToken, value);
                return this;
            }

            @Override
            public Replacer addExactMatchReplacements(Map<String, String> matchTokenToValue)
            {
                Optional.ofNullable(matchTokenToValue)
                        .ifPresent(map -> map.forEach(this::addExactMatchReplacement));
                return this;
            }

            @Override
            public Replacer addRegExMatchReplacement(String regEx, String replacement)
            {
                this.regexToReplacement.put(regEx, replacement);
                return this;
            }

            @Override
            public String findAndReplaceAllIn(String text)
            {
                String result = text;

                for (Map.Entry<String, String> exactMatchTokenAndValue : this.exactMatchTokenToValue.entrySet())
                {
                    result = org.apache.commons.lang3.StringUtils.replace(result, exactMatchTokenAndValue.getKey(), exactMatchTokenAndValue.getValue());
                }

                for (Map.Entry<String, String> regExAndReplacement : this.regexToReplacement.entrySet())
                {
                    result = org.apache.commons.lang3.RegExUtils.replaceAll(result, regExAndReplacement.getKey(), regExAndReplacement.getValue());
                }

                return result;
            }
        };
    }

    /**
     * @see #findAndReplaceAllIn(String)
     * @author omnaest
     */
    public static interface Replacer
    {
        public Replacer addExactMatchReplacement(String matchToken, String value);

        public Replacer addExactMatchReplacements(Map<String, String> matchTokenToValue);

        public Replacer addRegExMatchReplacement(String regEx, String replacement);

        public String findAndReplaceAllIn(String text);

    }
}
