package org.omnaest.utils.functional;

/**
 * Similar to {@link Runnable} but indicates a simple {@link Action} to be taken instead of being dedicated to a thread pool.
 * 
 * @author omnaest
 */
@FunctionalInterface
public interface Action extends Runnable
{
}