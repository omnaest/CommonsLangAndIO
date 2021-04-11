package org.omnaest.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Helper for {@link Optional}s
 * 
 * @author omnaest
 */
public class OptionalUtils
{
    /**
     * Returns the first non empty given {@link Optional} or {@link Optional#empty()} otherwise
     * 
     * @param optionals
     * @return
     */
    @SafeVarargs
    public static <E> Optional<E> takeFirstNonEmpty(Optional<E>... optionals)
    {
        return Optional.ofNullable(optionals)
                       .map(Arrays::asList)
                       .orElse(Collections.emptyList())
                       .stream()
                       .filter(Optional::isPresent)
                       .findFirst()
                       .orElse(Optional.empty());
    }
}
