package org.omnaest.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.optional.BiOptional;

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

    /**
     * Similar to {@link #both(Optional, Optional)} but wraps the given arguments using {@link Optional#ofNullable(Object)}
     * 
     * @param first
     * @param second
     * @return
     */
    public static <O1, O2> Optional<BiElement<O1, O2>> bothOfNullable(O1 first, O2 second)
    {
        return both(Optional.ofNullable(first), Optional.ofNullable(second));
    }

    /**
     * Returns an {@link Optional} of a {@link BiElement} containing both values of each individual {@link Optional#get()}, only if both {@link Optional}s have
     * values present.
     * 
     * @param firstOptional
     * @param secondOptional
     * @return
     */
    public static <O1, O2> Optional<BiElement<O1, O2>> both(Optional<O1> firstOptional, Optional<O2> secondOptional)
    {
        return Optional.of(BiElement.of(firstOptional, secondOptional))
                       .filter(bi -> bi.hasNoNullValue())
                       .filter(bi -> bi.getFirst()
                                       .isPresent())
                       .filter(bi -> bi.getSecond()
                                       .isPresent())
                       .map(bi -> bi.applyToFirstArgument(Optional::get))
                       .map(bi -> bi.applyToSecondArgument(Optional::get));
    }

    /**
     * Returns a {@link BiOptional} for the two given {@link Optional}s
     * 
     * @param firstOptional
     * @param secondOptional
     * @return
     */
    public static <O1, O2> BiOptional<O1, O2> both2(Optional<O1> firstOptional, Optional<O2> secondOptional)
    {
        return BiOptional.of(firstOptional, secondOptional);
    }

    /**
     * Returns a {@link BiOptional} for the two given values (which can be null)
     * 
     * @param firstValue
     * @param secondValue
     * @return
     */
    public static <E1, E2> BiOptional<E1, E2> bothOfNullable2(E1 firstValue, E2 secondValue)
    {
        return BiOptional.ofNullable(firstValue, secondValue);
    }
}
