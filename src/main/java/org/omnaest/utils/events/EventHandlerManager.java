package org.omnaest.utils.events;

/**
 * The {@link EventHandlerManager} allows to act as {@link EventHandlerRegistry} and can consume events as {@link EventHandler} which it distributes to the
 * registered {@link EventHandler} instances.
 * 
 * @author omnaest
 * @param <E>
 */
public interface EventHandlerManager<E> extends EventHandler<E>, EventHandlerRegistry<E>
{

}
