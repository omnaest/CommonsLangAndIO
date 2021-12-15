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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.functional.Provider;

/**
 * Helper for {@link MatchFinderFactory} operations
 * 
 * @author omnaest
 */
public class MatcherUtils
{
    /**
     * @see #of(Pattern)
     * @author omnaest
     */
    public static interface MatchFinderFactory
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

        public MatchResult withNoCaching();

        /**
         * Returns the {@link Match#getMatchRegion()} tokens of all {@link Match}es as {@link Set}
         * 
         * @return
         */
        public Set<String> getMatchedTokens();
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

    /**
     * Builder of a {@link MatchFinder}
     * 
     * @see #build()
     * @author omnaest
     */
    public static interface MatchFinderBuilder
    {

        /**
         * Matches a given regEx like {@link #of(Pattern)}
         * 
         * @see #of(Pattern)
         * @param regEx
         * @return
         */
        public MatchFinderBuilder ofRegEx(String regEx);

        /**
         * Matches the exact {@link String}
         * 
         * @param str
         * @return
         */
        public MatchFinderBuilder ofExact(String str);

        /**
         * Matches any of the given {@link String}s
         * 
         * @param exactMatchTokens
         * @return
         */
        public MatchFinderBuilder ofAnyExact(Collection<String> exactMatchTokens);

        /**
         * Similar to {@link #ofAnyExact(Collection)}
         * 
         * @param exactMatchTokens
         * @return
         */
        public MatchFinderBuilder ofAnyExact(String... exactMatchTokens);

        /**
         * Adds a regular expression suffix match token to all the
         * 
         * @param regExSuffix
         * @return
         */
        public MatchFinderBuilder withRegExSuffix(String regExSuffix);

        /**
         * Similar to {@link #withRegExSuffix(String)} but for exact words and no as regular expression treated suffix tokens
         * 
         * @param exactSuffix
         * @return
         */
        public MatchFinderBuilder withExactSuffix(String exactSuffix);

        /**
         * Similar to {@link #withExactSuffix(String)} but the whole token is marked as optional
         * 
         * @param exactSuffix
         * @return
         */
        public MatchFinderBuilder withExactOptionalSuffix(String exactSuffix);

        /**
         * Similar to {@link #withExactOptionalSuffix(String)} for multiple suffix tokens which are applied in an OR conjunction.
         * 
         * @param exactSuffix
         * @return
         */
        public MatchFinderBuilder withExactOptionalSuffixes(String... exactSuffix);

        /**
         * Creates a {@link MatchFinder} instance
         * 
         * @return
         */
        public MatchFinder build();

        MatchFinderBuilder withRegExOptionalSuffix(String regExSuffix);
    }

    /**
     * Creates a {@link MatchFinderBuilder} which allows to combine exact and regular expression matches in a OR conjunction
     * 
     * @se {@link #matcher()}
     * @return
     */
    public static MatchFinderBuilder matcherBuilder()
    {
        return new MatchFinderBuilder()
        {
            private List<String> regExParts            = new ArrayList<>();
            private List<String> regExSuffixes         = new ArrayList<>();
            private List<String> regExOptionalSuffixes = new ArrayList<>();

            @Override
            public MatchFinderBuilder ofRegEx(String regEx)
            {
                this.regExParts.add(regEx);
                return this;
            }

            @Override
            public MatchFinderBuilder ofExact(String str)
            {
                return this.ofAnyExact(Arrays.asList(str));
            }

            @Override
            public MatchFinderBuilder ofAnyExact(Collection<String> exactMatchTokens)
            {
                this.regExParts.addAll(Optional.ofNullable(exactMatchTokens)
                                               .orElse(Collections.emptyList())
                                               .stream()
                                               .map(token -> Pattern.quote(token))
                                               .collect(Collectors.toList()));
                return this;
            }

            @Override
            public MatchFinderBuilder ofAnyExact(String... exactMatchTokens)
            {
                return this.ofAnyExact(Arrays.asList(exactMatchTokens));
            }

            @Override
            public MatchFinderBuilder withRegExSuffix(String regExSuffix)
            {
                Optional.ofNullable(regExSuffix)
                        .ifPresent(this.regExSuffixes::add);
                return this;
            }

            @Override
            public MatchFinderBuilder withRegExOptionalSuffix(String regExSuffix)
            {
                Optional.ofNullable(regExSuffix)
                        .ifPresent(this.regExOptionalSuffixes::add);
                return this;
            }

            @Override
            public MatchFinderBuilder withExactSuffix(String exactSuffix)
            {
                return this.withRegExSuffix(Pattern.quote(exactSuffix));
            }

            @Override
            public MatchFinderBuilder withExactOptionalSuffix(String exactSuffix)
            {
                return this.withRegExOptionalSuffix(this.encloseInNonCapturingGroup(Pattern.quote(exactSuffix)));
            }

            @Override
            public MatchFinderBuilder withExactOptionalSuffixes(String... exactSuffixes)
            {
                Optional.ofNullable(exactSuffixes)
                        .map(Arrays::asList)
                        .orElse(Collections.emptyList())
                        .forEach(this::withExactOptionalSuffix);
                return this;
            }

            @Override
            public MatchFinder build()
            {
                String suffixRegEx = this.regExSuffixes.isEmpty() ? ""
                        : this.encloseInNonCapturingGroup(this.regExSuffixes.stream()
                                                                            .map(this::encloseInNonCapturingGroup)
                                                                            .collect(Collectors.joining("|")));
                String optionalSuffixRegEx = this.regExOptionalSuffixes.isEmpty() ? ""
                        : this.encloseInNonCapturingGroup(this.regExOptionalSuffixes.stream()
                                                                                    .map(this::encloseInNonCapturingGroup)
                                                                                    .collect(Collectors.joining("|")))
                                + "?";
                String regEx = this.regExParts.stream()
                                              .map(part -> part + suffixRegEx + optionalSuffixRegEx)
                                              .map(this::encloseInNonCapturingGroup)
                                              .collect(Collectors.joining("|"));
                return matcher().ofRegEx(regEx);
            }

            private String encloseInNonCapturingGroup(String token)
            {
                return "(?:" + token + ")";
            }

        };
    }

    /**
     * Creates a {@link MatchFinderFactory} which allows to create a {@link MatchFinder} instance. For combination of multiple tokens please consider using
     * {@link #matcherBuilder()} instead.
     * 
     * @see #matcherBuilder()
     * @return
     */
    public static MatchFinderFactory matcher()
    {
        return new MatchFinderFactory()
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
                            BooleanSupplier matcherAction = () -> matcher.matches();
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

                            @Override
                            public Set<String> getMatchedTokens()
                            {
                                return this.stream()
                                           .map(match -> match.getMatchRegion())
                                           .collect(Collectors.toSet());
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
                                    BooleanSupplier matcherAction = () -> matcher.find();
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
                            BooleanSupplier matcherAction = () -> matcher.find();
                            retval = this.determineMatches(input, matcher, matcherAction);
                        }
                        else
                        {
                            retval = Optional.empty();
                        }
                        return retval;
                    }

                    private Optional<Stream<Match>> determineMatches(String input, Matcher matcher, BooleanSupplier matcherAction)
                    {
                        Iterator<Match> iterator = new Iterator<Match>()
                        {
                            private CachedElement<Match> matchCache = CachedElement.of(() -> this.determineNextMatch()
                                                                                                 .orElse(null));

                            @Override
                            public boolean hasNext()
                            {
                                return this.matchCache.get() != null;
                            }

                            private Optional<Match> determineNextMatch()
                            {
                                return MatcherUtils.wrapIntoMatch(input, matcher, matcherAction);
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

    private static Optional<Match> wrapIntoMatch(String input, Matcher matcher, BooleanSupplier matcherAction)
    {
        Optional<Match> retval = Optional.empty();

        if (matcherAction.getAsBoolean())
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

            retval = Optional.of(new Match()
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

            });
        }

        return retval;
    }

    public static Replacer replacer()
    {
        return new Replacer()
        {
            private Map<String, UnaryOperator<String>>   exactMatchTokenToValue = new LinkedHashMap<>();
            private Map<String, Function<Match, String>> regexToReplacement     = new LinkedHashMap<>();

            @Override
            public Replacer addExactMatchReplacement(String matchToken, String value)
            {
                return this.addExactMatchReplacement(matchToken, () -> value);
            }

            @Override
            public Replacer addExactMatchReplacement(String matchToken, Supplier<String> valueSupplier)
            {
                return this.addExactMatchReplacement(matchToken, value -> valueSupplier.get());
            }

            @Override
            public Replacer addExactMatchReplacement(String matchToken, UnaryOperator<String> valueSupplier)
            {
                this.exactMatchTokenToValue.put(matchToken, valueSupplier);
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
                return this.addRegExMatchReplacement(regEx, () -> replacement);
            }

            @Override
            public Replacer addRegExMatchReplacement(String regEx, Supplier<String> replacementSupplier)
            {
                return this.addRegExMatchReplacement(regEx, value -> replacementSupplier.get());
            }

            @Override
            public Replacer addRegExMatchReplacement(String regEx, Function<Match, String> replacementSupplier)
            {
                this.regexToReplacement.put(regEx, replacementSupplier);
                return this;
            }

            @Override
            public String findAndReplaceAllIn(String text)
            {
                String result = text;

                for (Map.Entry<String, UnaryOperator<String>> exactMatchTokenAndValue : this.exactMatchTokenToValue.entrySet())
                {
                    String token = exactMatchTokenAndValue.getKey();
                    int numberOfMatches = org.apache.commons.lang3.StringUtils.countMatches(result, token);
                    if (numberOfMatches >= 1)
                    {
                        result = org.apache.commons.lang3.StringUtils.replace(result, token, exactMatchTokenAndValue.getValue()
                                                                                                                    .apply(token));
                    }
                }

                for (Map.Entry<String, Function<Match, String>> regExAndReplacement : this.regexToReplacement.entrySet())
                {

                    Matcher matcher = Pattern.compile(regExAndReplacement.getKey())
                                             .matcher(result);
                    StringBuffer sb = new StringBuffer();

                    String input = result;
                    StreamUtils.takeOptionalUntilEmpty(() -> wrapIntoMatch(input, matcher, () -> matcher.find()))
                               .forEach(match ->
                               {
                                   String replacement = regExAndReplacement.getValue()
                                                                           .apply(match);
                                   matcher.appendReplacement(sb, replacement);
                               });
                    matcher.appendTail(sb);
                    result = sb.toString();
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

        public Replacer addExactMatchReplacement(String matchToken, Supplier<String> valueSupplier);

        public Replacer addExactMatchReplacement(String matchToken, UnaryOperator<String> valueSupplier);

        public Replacer addExactMatchReplacements(Map<String, String> matchTokenToValue);

        public Replacer addRegExMatchReplacement(String regEx, String replacement);

        public Replacer addRegExMatchReplacement(String regEx, Supplier<String> replacementSupplier);

        public Replacer addRegExMatchReplacement(String regEx, Function<Match, String> replacementSupplier);

        public String findAndReplaceAllIn(String text);

    }

    /**
     * @see TokenInterpreter
     * @return
     */
    public static TokenInterpreter interpreter()
    {
        Replacer replacer = MatcherUtils.replacer();
        return new TokenInterpreter()
        {
            private AtomicBoolean           operationHasRun = new AtomicBoolean(false);
            private Optional<ElseOperation> elseOperation   = Optional.empty();

            @Override
            public TokenInterpreter ifContainsExact(String matchToken, TokenOperation tokenOperation)
            {
                replacer.addExactMatchReplacement(matchToken, match ->
                {
                    tokenOperation.accept(match);
                    this.markOperationHasRun();
                    return "";
                });
                return this;
            }

            private void markOperationHasRun()
            {
                this.operationHasRun.set(true);
            }

            @Override
            public TokenInterpreter ifContainsRegEx(String regEx, RegExOperation tokenOperation)
            {
                replacer.addRegExMatchReplacement(regEx, match ->
                {
                    tokenOperation.accept(match);
                    this.markOperationHasRun();
                    return "";
                });
                return this;
            }

            @Override
            public void accept(String text)
            {
                this.apply(text);
            }

            @Override
            public String apply(String text)
            {
                String result = replacer.findAndReplaceAllIn(text);
                this.elseOperation.filter(o -> !this.operationHasRun.get())
                                  .ifPresent(ElseOperation::run);
                return result;
            }

            @Override
            public TokenInterpreter ifStartsWith(String matchToken, TokenOperation tokenOperation)
            {
                return this.ifContainsRegEx("^" + Pattern.quote(matchToken), match -> tokenOperation.accept(match.getMatchRegion()));
            }

            @Override
            public TokenInterpreter orElse(ElseOperation elseOperation)
            {
                this.elseOperation = Optional.ofNullable(elseOperation);
                return this;
            }
        };
    }

    /**
     * A {@link TokenInterpreter} utilizes {@link Pattern} matching routines to interpret tokens within a given {@link String}.<br>
     * <br>
     * The {@link #accept(String)} will just interpret a given {@link String}, while {@link #apply(String)} will interpret the given {@link String} and return
     * the input {@link String} but all the matched token being removed.
     * 
     * @author omnaest
     */
    public static interface TokenInterpreter extends Consumer<String>, Function<String, String>
    {

        public TokenInterpreter ifContainsRegEx(String regEx, RegExOperation tokenOperation);

        public TokenInterpreter ifContainsExact(String matchToken, TokenOperation tokenOperation);

        public TokenInterpreter ifStartsWith(String matchToken, TokenOperation tokenOperation);

        public TokenInterpreter orElse(ElseOperation elseOperation);

        public static interface TokenOperation extends Consumer<String>
        {

        }

        public static interface RegExOperation extends Consumer<Match>
        {

        }

        public static interface ElseOperation extends Runnable
        {

        }
    }
}
