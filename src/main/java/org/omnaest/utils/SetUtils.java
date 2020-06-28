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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helpoer methods for {@link Set}s
 *
 * @author Omnaest
 */
public class SetUtils
{
    @SafeVarargs
    public static <E> Set<E> merge(Collection<E>... collections)
    {
        return Arrays.asList(collections)
                     .stream()
                     .filter(collection -> collection != null)
                     .flatMap(collection -> collection.stream())
                     .collect(Collectors.toSet());
    }

    public static <E> E last(Set<E> set)
    {
        E retval = null;
        for (E element : set)
        {
            retval = element;
        }
        return retval;
    }

    public static <E> E first(Set<E> set)
    {
        E retval = null;
        if (set != null && !set.isEmpty())
        {
            retval = set.iterator()
                        .next();
        }
        return retval;
    }

    public static <E> Set<E> addToNew(Set<E> set, E element)
    {
        Set<E> retset = set != null ? new LinkedHashSet<>(set) : new LinkedHashSet<>();
        retset.add(element);
        return retset;
    }

    /**
     * Makes a copy of the given {@link Set} and removes the provided elements from the new {@link Set}
     * 
     * @param set
     * @param elements
     * @return new {@link LinkedHashSet}
     */
    @SafeVarargs
    public static <E> Set<E> copyAndRemove(Set<E> set, E... elements)
    {
        Set<E> retset = set != null ? new LinkedHashSet<>(set) : new LinkedHashSet<>();
        if (elements != null)
        {
            retset.removeAll(Arrays.asList(elements));
        }
        return retset;
    }

    /**
     * @see #getAdded()
     * @see #getRemoved()
     * @see #getShared()
     * @author omnaest
     * @param <E>
     */
    public static class SetDelta<E>
    {
        private Set<E> added;
        private Set<E> removed;
        private Set<E> shared;

        public SetDelta(Set<E> added, Set<E> removed, Set<E> shared)
        {
            super();
            this.added = added;
            this.removed = removed;
            this.shared = shared;
        }

        /**
         * Returns a {@link Set} of elements added between the previous and after {@link Set}s
         * 
         * @return
         */
        public Set<E> getAdded()
        {
            return this.added;
        }

        /**
         * Returns a {@link Set} of elements removed between the previous and after {@link Set}s
         * 
         * @return
         */
        public Set<E> getRemoved()
        {
            return this.removed;
        }

        /**
         * Returns a {@link Set} of elements shared between the previous and after {@link Set}s
         * 
         * @return
         */
        public Set<E> getShared()
        {
            return this.shared;
        }

        @Override
        public String toString()
        {
            return "SetDelta [added=" + this.added + ", removed=" + this.removed + ", shared=" + this.shared + "]";
        }

        public Set<E> getAll()
        {
            return StreamUtils.concat(this.added.stream(), this.shared.stream(), this.removed.stream())
                              .collect(Collectors.toSet());
        }

    }

    /**
     * Returns a {@link SetDelta} of two given {@link Set}s
     * 
     * @param previous
     * @param next
     * @return
     */
    public static <E> SetDelta<E> delta(Collection<E> previous, Collection<E> next)
    {
        Set<E> added = new HashSet<>(next);
        added.removeAll(previous);

        Set<E> removed = new HashSet<>(previous);
        removed.removeAll(next);

        Set<E> shared = new HashSet<>();
        shared.addAll(previous);
        shared.addAll(next);
        shared.removeAll(added);
        shared.removeAll(removed);

        return new SetDelta<>(added, removed, shared);
    }

    /**
     * @see ListUtils#getRandomElement(java.util.List)
     * @param collection
     * @return
     */
    public static <E> Optional<E> getRandomElement(Collection<E> collection)
    {
        return ListUtils.getRandomElement(collection.stream()
                                                    .collect(Collectors.toList()));
    }

    /**
     * @see ListUtils#getRandomElementStream(Collection)
     * @param collection
     * @return
     */
    public static <E> Stream<E> getRandomElementStream(Collection<E> collection)
    {
        return ListUtils.getRandomElementStream(collection);
    }

    public static <E> Set<E> empty()
    {
        return Collections.emptySet();
    }

    @SafeVarargs
    public static <E> Set<E> toSet(E... elements)
    {
        return Arrays.asList(elements)
                     .stream()
                     .collect(Collectors.toSet());
    }
}
