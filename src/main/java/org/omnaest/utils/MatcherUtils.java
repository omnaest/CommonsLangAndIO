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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.omnaest.utils.element.CachedElement;

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
		public Optional<Stream<Match>> findIn(String input);

		public Optional<Stream<Match>> matchAgainst(String input);
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
					public Optional<Stream<Match>> matchAgainst(String input)
					{
						Matcher matcher = pattern.matcher(input);
						Supplier<Boolean> matcherAction = () -> matcher.matches();
						return this.determineMatches(input, matcher, matcherAction);
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
									for (int ii = 0; ii <= matcher.groupCount(); ii++)
									{
										groups.put(ii, matcher.group(ii));
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
										public String replaceWith(String replacement)
										{
											return this	.asReplacer(() -> replacement)
														.apply(input);
										}

										@Override
										public RegionReplacer asReplacer(Supplier<String> replacements)
										{
											return input -> StringUtils	.builder()
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
